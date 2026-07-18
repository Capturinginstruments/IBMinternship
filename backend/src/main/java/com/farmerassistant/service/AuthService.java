package com.farmerassistant.service;

import com.farmerassistant.config.EmailService;
import com.farmerassistant.dto.request.*;
import com.farmerassistant.dto.response.AuthResponse;
import com.farmerassistant.dto.response.UserDto;
import com.farmerassistant.exception.DuplicateResourceException;
import com.farmerassistant.exception.InvalidTokenException;
import com.farmerassistant.exception.ResourceNotFoundException;
import com.farmerassistant.model.*;
import com.farmerassistant.repository.*;
import com.farmerassistant.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final FarmerRepository farmerRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RateLimitingService rateLimitingService;

    private static final Random RANDOM = new SecureRandom();

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with email '" + request.getEmail() + "' already exists.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(request.getRole() != null ? request.getRole() : User.Role.FARMER)
                .isActive(true)
                .isEmailVerified(false)
                .build();

        user = userRepository.save(user);

        if (user.getRole() == User.Role.FARMER) {
            Farmer farmer = Farmer.builder().user(user).build();
            farmerRepository.save(farmer);
        }

        String otp = generateOtp();
        saveOtp(user, otp, OtpToken.OtpPurpose.EMAIL_VERIFY);
        emailService.sendOtpEmail(user.getEmail(), otp, "EMAIL_VERIFY");
        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());

        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null,
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().name())));

        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());
        saveRefreshToken(user, refreshToken);

        return buildAuthResponse(accessToken, refreshToken, user);
    }

    public AuthResponse login(LoginRequest request) {
        if (rateLimitingService.isLoginBlocked(request.getEmail())) {
            throw new com.farmerassistant.exception.AccessForbiddenException("Too many failed login attempts. Your account is locked for 15 minutes.");
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if (!user.isActive()) {
                throw new BadCredentialsException("Account is deactivated. Please contact support.");
            }

            refreshTokenRepository.deleteByUserId(user.getId());

            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());
            saveRefreshToken(user, refreshToken);

            rateLimitingService.resetLoginAttempts(request.getEmail());
            return buildAuthResponse(accessToken, refreshToken, user);
        } catch (BadCredentialsException e) {
            rateLimitingService.recordFailedLogin(request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    public AuthResponse refreshToken(String refreshTokenStr) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new InvalidTokenException("Refresh token has expired. Please login again.");
        }

        User user = token.getUser();
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        token.setToken(newRefreshToken);
        token.setExpiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getAccessTokenExpiry() / 1000 * 7 * 24));
        refreshTokenRepository.save(token);

        return buildAuthResponse(newAccessToken, newRefreshToken, user);
    }

    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            otpTokenRepository.deleteByUserIdAndPurpose(user.getId(), OtpToken.OtpPurpose.PASSWORD_RESET);
            String otp = generateOtp();
            saveOtp(user, otp, OtpToken.OtpPurpose.PASSWORD_RESET);
            emailService.sendOtpEmail(email, otp, "PASSWORD_RESET");
            log.info("Password reset OTP sent to: {}", email);
        });
    }

    public void resetPassword(ResetPasswordRequest request) {
        if (rateLimitingService.isOtpBlocked(request.getEmail())) {
            throw new com.farmerassistant.exception.AccessForbiddenException("Too many invalid OTP attempts. Verification is locked for 10 minutes.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        OtpToken otpToken = otpTokenRepository.findValidOtp(
                user.getId(), OtpToken.OtpPurpose.PASSWORD_RESET, LocalDateTime.now())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired OTP. Please request a new one."));

        if (!otpToken.getOtp().equals(request.getOtp())) {
            rateLimitingService.recordFailedOtp(request.getEmail());
            throw new InvalidTokenException("Invalid OTP provided.");
        }

        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        refreshTokenRepository.deleteByUserId(user.getId());
        rateLimitingService.resetOtpAttempts(request.getEmail());
        log.info("Password reset successful for user: {}", user.getEmail());
    }

    public void verifyEmail(String email, String otp) {
        if (rateLimitingService.isOtpBlocked(email)) {
            throw new com.farmerassistant.exception.AccessForbiddenException("Too many invalid OTP attempts. Verification is locked for 10 minutes.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        OtpToken otpToken = otpTokenRepository.findValidOtp(
                user.getId(), OtpToken.OtpPurpose.EMAIL_VERIFY, LocalDateTime.now())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired OTP."));

        if (!otpToken.getOtp().equals(otp)) {
            rateLimitingService.recordFailedOtp(email);
            throw new InvalidTokenException("Invalid OTP provided.");
        }

        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);

        user.setEmailVerified(true);
        userRepository.save(user);
        rateLimitingService.resetOtpAttempts(email);
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }

    public UserDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toUserDto(user);
    }

    // ─── Private helpers ───────────────────────────────────────────────

    private String generateOtp() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private void saveOtp(User user, String otp, OtpToken.OtpPurpose purpose) {
        OtpToken otpToken = OtpToken.builder()
                .user(user)
                .otp(otp)
                .purpose(purpose)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .isUsed(false)
                .build();
        otpTokenRepository.save(otpToken);
    }

    private void saveRefreshToken(User user, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiry())
                .user(toUserDto(user))
                .build();
    }

    public static UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole())
                .profileImageUrl(user.getProfileImageUrl())
                .isEmailVerified(user.isEmailVerified())
                .isActive(user.isActive())
                .build();
    }
}
