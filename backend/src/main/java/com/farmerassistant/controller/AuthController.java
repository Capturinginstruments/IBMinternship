package com.farmerassistant.controller;

import com.farmerassistant.dto.request.LoginRequest;
import com.farmerassistant.dto.request.ResetPasswordRequest;
import com.farmerassistant.dto.request.SignupRequest;
import com.farmerassistant.dto.response.ApiResponse;
import com.farmerassistant.dto.response.AuthResponse;
import com.farmerassistant.dto.response.UserDto;
import com.farmerassistant.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "JWT Authentication APIs")
public class AuthController {

    private final AuthService authService;
    private final com.farmerassistant.service.RateLimitingService rateLimitingService;
    private final com.farmerassistant.security.JwtTokenProvider jwtTokenProvider;
    private final jakarta.servlet.http.HttpServletRequest httpServletRequest;

    @PostMapping("/signup")
    @Operation(summary = "Register a new user (farmer/admin/officer)")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully. Please verify your email.", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and get JWT tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("refreshToken is required"));
        }
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request OTP for password reset")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestBody Map<String, String> body) {
        authService.forgotPassword(body.get("email"));
        return ResponseEntity.ok(ApiResponse.success(
                "If an account with this email exists, an OTP has been sent.", null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using OTP")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully. Please login.", null));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email with OTP")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestBody Map<String, String> body) {
        authService.verifyEmail(body.get("email"), body.get("otp"));
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully.", null));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and invalidate refresh token")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody Map<String, String> body) {
        authService.logout(body.get("refreshToken"));
        
        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            rateLimitingService.blacklistJwt(jwt, jwtTokenProvider.getAccessTokenExpiry());
        }
        
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully.", null));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user info")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserDto user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
