package com.farmerassistant.controller;

import com.farmerassistant.dto.response.ApiResponse;
import com.farmerassistant.dto.response.DiseaseReportResponse;
import com.farmerassistant.repository.UserRepository;
import com.farmerassistant.service.DiseaseDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/disease")
@RequiredArgsConstructor
@Tag(name = "Disease Detection", description = "Plant disease detection using AI")
public class DiseaseController {

    private final DiseaseDetectionService diseaseService;
    private final UserRepository userRepository;

    @PostMapping(value = "/detect", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('FARMER','OFFICER','ADMIN')")
    @Operation(summary = "Detect plant disease from crop image")
    public ResponseEntity<ApiResponse<DiseaseReportResponse>> detect(
            @RequestPart("image") MultipartFile image,
            @RequestPart(value = "cropType", required = false) String cropType,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {
        Long userId = getUserId(userDetails);
        DiseaseReportResponse response = diseaseService.detect(image, cropType, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Disease analysis complete", response));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('FARMER','OFFICER','ADMIN')")
    @Operation(summary = "Get disease detection history")
    public ResponseEntity<ApiResponse<Page<DiseaseReportResponse>>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        Page<DiseaseReportResponse> history = diseaseService.getHistory(userId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('FARMER','OFFICER','ADMIN')")
    @Operation(summary = "Mark a disease report as resolved")
    public ResponseEntity<ApiResponse<DiseaseReportResponse>> resolve(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Report marked as resolved",
                diseaseService.markResolved(id, userId)));
    }

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
    }
}
