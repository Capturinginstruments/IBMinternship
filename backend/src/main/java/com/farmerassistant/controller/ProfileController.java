package com.farmerassistant.controller;

import com.farmerassistant.config.S3Config;
import com.farmerassistant.dto.request.UpdateProfileRequest;
import com.farmerassistant.dto.response.ApiResponse;
import com.farmerassistant.dto.response.DashboardResponse;
import com.farmerassistant.dto.response.UserDto;
import com.farmerassistant.model.*;
import com.farmerassistant.repository.*;
import com.farmerassistant.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "User profile and dashboard APIs")
public class ProfileController {

    private final UserRepository userRepository;
    private final FarmerRepository farmerRepository;
    private final DiseaseReportRepository diseaseReportRepository;
    private final NotificationRepository notificationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final GovernmentSchemeRepository schemeRepository;
    private final MarketPriceRepository marketPriceRepository;
    private final WeatherService weatherService;
    private final CropRecommendationRepository cropRecRepository;
    private final S3Service s3Service;
    private final AuthService authService;

    @GetMapping
    @Operation(summary = "Get full current user profile")
    public ResponseEntity<ApiResponse<UserDto>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new com.farmerassistant.exception.ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));
        return ResponseEntity.ok(ApiResponse.success(AuthService.toUserDto(user)));
    }

    @PutMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Update profile and optionally upload profile image")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            @RequestPart(value = "data") @jakarta.validation.Valid UpdateProfileRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new com.farmerassistant.exception.ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());

        if (profileImage != null && !profileImage.isEmpty()) {
            String imageUrl = s3Service.uploadFile(profileImage, "profile-images");
            user.setProfileImageUrl(imageUrl);
        }
        user = userRepository.save(user);

        Farmer farmer = farmerRepository.findByUserId(user.getId()).orElse(null);
        if (farmer != null && user.getRole() == User.Role.FARMER) {
            if (request.getState() != null) farmer.setState(request.getState());
            if (request.getDistrict() != null) farmer.setDistrict(request.getDistrict());
            if (request.getVillage() != null) farmer.setVillage(request.getVillage());
            if (request.getLandAcres() != null) farmer.setLandAcres(request.getLandAcres());
            if (request.getSoilType() != null) {
                try { farmer.setSoilType(Farmer.SoilType.valueOf(request.getSoilType().toUpperCase())); }
                catch (Exception ignored) {}
            }
            if (request.getPrimaryCrop() != null) farmer.setPrimaryCrop(request.getPrimaryCrop());
            if (request.getSecondaryCrop() != null) farmer.setSecondaryCrop(request.getSecondaryCrop());
            if (request.getWaterSource() != null) farmer.setWaterSource(request.getWaterSource());
            if (request.getLatitude() != null) farmer.setLatitude(request.getLatitude());
            if (request.getLongitude() != null) farmer.setLongitude(request.getLongitude());
            farmerRepository.save(farmer);
        }

        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully",
                AuthService.toUserDto(user)));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get farmer dashboard summary data")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new com.farmerassistant.exception.ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));
        Long userId = user.getId();

        // ── Parallel Execution of Independent Tasks ────────────────────────────────
        var unreadNotifsFuture = java.util.concurrent.CompletableFuture.supplyAsync(() ->
                notificationRepository.countByUserIdAndIsReadFalse(userId));

        var unresolvedDiseasesFuture = java.util.concurrent.CompletableFuture.supplyAsync(() ->
                diseaseReportRepository.countByUserIdAndIsResolvedFalse(userId));

        var farmerFuture = java.util.concurrent.CompletableFuture.supplyAsync(() ->
                farmerRepository.findByUserId(userId).orElse(null));

        var recentRecsFuture = java.util.concurrent.CompletableFuture.supplyAsync(() ->
                cropRecRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId));

        var sessionsFuture = java.util.concurrent.CompletableFuture.supplyAsync(() ->
                chatMessageRepository.findDistinctSessionIdsByUserId(userId));

        var schemesFuture = java.util.concurrent.CompletableFuture.supplyAsync(() ->
                schemeRepository.findByIsActiveTrue(PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdAt"))));

        // Wait for basic tasks to finish
        java.util.concurrent.CompletableFuture.allOf(
                unreadNotifsFuture, unresolvedDiseasesFuture, farmerFuture,
                recentRecsFuture, sessionsFuture, schemesFuture
        ).join();

        DashboardResponse.DashboardResponseBuilder builder = DashboardResponse.builder();
        
        try {
            builder.unreadNotifications(unreadNotifsFuture.get());
            builder.unresolvedDiseaseAlerts(unresolvedDiseasesFuture.get());
            
            Farmer farmer = farmerFuture.get();
            if (farmer != null) {
                builder.farmerProfile(DashboardResponse.FarmerProfileSummary.builder()
                        .fullName(user.getFirstName() + " " + user.getLastName())
                        .profileImageUrl(user.getProfileImageUrl())
                        .primaryCrop(farmer.getPrimaryCrop())
                        .district(farmer.getDistrict()).state(farmer.getState()).build());

                // Weather and Market price can be fetched concurrently
                var weatherFuture = java.util.concurrent.CompletableFuture.runAsync(() -> {
                    if (farmer.getLatitude() != null && farmer.getLongitude() != null) {
                        try {
                            var weather = weatherService.getWeatherByCoords(farmer.getLatitude(), farmer.getLongitude());
                            builder.weather(DashboardResponse.WeatherSummary.builder()
                                    .city(weather.getCity()).temperature(weather.getTemperature())
                                    .description(weather.getDescription()).iconCode(weather.getIconCode())
                                    .humidity(weather.getHumidity()).build());
                        } catch (Exception ignored) {}
                    }
                });

                var marketFuture = java.util.concurrent.CompletableFuture.runAsync(() -> {
                    if (farmer.getPrimaryCrop() != null && farmer.getState() != null) {
                        String searchCommodity = farmer.getPrimaryCrop().toLowerCase().trim();
                        String searchState = farmer.getState().toLowerCase().trim();
                        var prices = marketPriceRepository.findByCommodityAndState(searchCommodity, searchState);
                        if (!prices.isEmpty()) {
                            var mp = prices.get(0);
                            builder.marketSummary(DashboardResponse.MarketSummary.builder()
                                    .commodity(mp.getCommodity()).modalPrice(mp.getModalPrice())
                                    .priceUnit(mp.getPriceUnit()).build());
                        }
                    }
                });

                java.util.concurrent.CompletableFuture.allOf(weatherFuture, marketFuture).join();
            }

            var recentRecs = recentRecsFuture.get();
            if (!recentRecs.isEmpty()) {
                var rec = recentRecs.get(0);
                builder.latestCropRecommendation(
                    com.farmerassistant.dto.response.CropRecommendationResponse.builder()
                        .id(rec.getId()).recommendedCrop(rec.getRecommendedCrop())
                        .confidenceScore(rec.getConfidenceScore()).season(rec.getSeason())
                        .createdAt(rec.getCreatedAt()).build());
            }

            var sessions = sessionsFuture.get();
            List<DashboardResponse.ChatSessionSummary> chatSummaries = new ArrayList<>();
            for (int i = 0; i < Math.min(3, sessions.size()); i++) {
                String sid = sessions.get(i);
                ChatMessage last = chatMessageRepository.findLastMessageInSession(userId, sid);
                if (last != null) {
                    chatSummaries.add(DashboardResponse.ChatSessionSummary.builder()
                            .sessionId(sid)
                            .lastMessage(last.getMessage().length() > 60 ?
                                    last.getMessage().substring(0, 60) + "..." : last.getMessage())
                            .lastUpdated(last.getCreatedAt().toString()).build());
                }
            }
            builder.recentChats(chatSummaries);

            var schemes = schemesFuture.get();
            builder.featuredSchemes(schemes.getContent().stream().map(s ->
                    com.farmerassistant.dto.response.SchemeResponse.builder()
                            .id(s.getId()).title(s.getTitle()).category(s.getCategory())
                            .officialUrl(s.getOfficialUrl()).build())
                    .collect(Collectors.toList()));

        } catch (Exception ex) {
            throw new RuntimeException("Error assembling dashboard data", ex);
        }

        return ResponseEntity.ok(ApiResponse.success(builder.build()));
    }
}
