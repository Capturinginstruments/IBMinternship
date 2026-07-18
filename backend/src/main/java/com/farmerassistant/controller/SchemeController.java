package com.farmerassistant.controller;

import com.farmerassistant.dto.response.ApiResponse;
import com.farmerassistant.dto.response.SchemeResponse;
import com.farmerassistant.model.GovernmentScheme;
import com.farmerassistant.repository.UserRepository;
import com.farmerassistant.service.GovernmentSchemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.Map;

@RestController
@RequestMapping("/api/schemes")
@RequiredArgsConstructor
@Tag(name = "Government Schemes", description = "Agricultural government schemes APIs")
public class SchemeController {

    private final GovernmentSchemeService schemeService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get all active government schemes")
    public ResponseEntity<ApiResponse<Page<SchemeResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userDetails != null ? getUserId(userDetails) : null;
        return ResponseEntity.ok(ApiResponse.success(schemeService.getAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")), userId)));
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter schemes by category, state, crop, and land size")
    public ResponseEntity<ApiResponse<Page<SchemeResponse>>> filter(
            @RequestParam(required = false) GovernmentScheme.SchemeCategory category,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String crop,
            @RequestParam(required = false) Double minLand,
            @RequestParam(required = false) Double maxLand,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userDetails != null ? getUserId(userDetails) : null;
        return ResponseEntity.ok(ApiResponse.success(schemeService.getFiltered(
                category, state, crop, minLand, maxLand,
                PageRequest.of(page, size), userId)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search schemes by keyword")
    public ResponseEntity<ApiResponse<Page<SchemeResponse>>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userDetails != null ? getUserId(userDetails) : null;
        return ResponseEntity.ok(ApiResponse.success(
                schemeService.searchByKeyword(q, PageRequest.of(page, size), userId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single scheme by ID")
    public ResponseEntity<ApiResponse<SchemeResponse>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userDetails != null ? getUserId(userDetails) : null;
        return ResponseEntity.ok(ApiResponse.success(schemeService.getById(id, userId)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new government scheme (Admin only)")
    public ResponseEntity<ApiResponse<SchemeResponse>> create(
            @RequestBody GovernmentScheme scheme,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long adminId = getUserId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Scheme created successfully",
                        schemeService.create(scheme, adminId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a scheme (Admin only)")
    public ResponseEntity<ApiResponse<SchemeResponse>> update(
            @PathVariable Long id, @RequestBody GovernmentScheme scheme) {
        return ResponseEntity.ok(ApiResponse.success("Scheme updated", schemeService.update(id, scheme)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a scheme (Admin only)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        schemeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Scheme deactivated", null));
    }

    @PostMapping("/{id}/bookmark")
    @PreAuthorize("hasAnyRole('FARMER','OFFICER','ADMIN')")
    @Operation(summary = "Toggle bookmark for a scheme")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggleBookmark(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        boolean isBookmarked = schemeService.toggleBookmark(id, userId);
        String msg = isBookmarked ? "Scheme bookmarked" : "Bookmark removed";
        return ResponseEntity.ok(ApiResponse.success(msg, Map.of("isBookmarked", isBookmarked)));
    }

    @GetMapping("/bookmarks")
    @PreAuthorize("hasAnyRole('FARMER','OFFICER','ADMIN')")
    @Operation(summary = "Get user's bookmarked schemes")
    public ResponseEntity<ApiResponse<Page<SchemeResponse>>> getBookmarks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success(
                schemeService.getUserBookmarks(userId, PageRequest.of(page, size))));
    }

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new com.farmerassistant.exception.ResourceNotFoundException("User not found with email: " + userDetails.getUsername())).getId();
    }
}
