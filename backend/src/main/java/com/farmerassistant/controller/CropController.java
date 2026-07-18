package com.farmerassistant.controller;

import com.farmerassistant.dto.request.CropRecommendationRequest;
import com.farmerassistant.dto.response.ApiResponse;
import com.farmerassistant.dto.response.CropRecommendationResponse;
import com.farmerassistant.repository.UserRepository;
import com.farmerassistant.service.CropRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crops")
@RequiredArgsConstructor
@Tag(name = "Crop Recommendation", description = "AI-powered crop recommendation APIs")
public class CropController {

    private final CropRecommendationService cropService;
    private final UserRepository userRepository;

    @PostMapping("/recommend")
    @PreAuthorize("hasAnyRole('FARMER','OFFICER','ADMIN')")
    @Operation(summary = "Get AI crop recommendation based on soil and climate data")
    public ResponseEntity<ApiResponse<CropRecommendationResponse>> recommend(
            @Valid @RequestBody CropRecommendationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        CropRecommendationResponse response = cropService.recommend(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Crop recommendation generated successfully", response));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('FARMER','OFFICER','ADMIN')")
    @Operation(summary = "Get crop recommendation history")
    public ResponseEntity<ApiResponse<Page<CropRecommendationResponse>>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        Page<CropRecommendationResponse> history = cropService.getHistory(userId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/history/{id}")
    @PreAuthorize("hasAnyRole('FARMER','OFFICER','ADMIN')")
    @Operation(summary = "Get single crop recommendation by ID")
    public ResponseEntity<ApiResponse<CropRecommendationResponse>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success(cropService.getById(id, userId)));
    }

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
    }
}
