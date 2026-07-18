package com.farmerassistant.service;

import com.farmerassistant.dto.request.LoginRequest;
import com.farmerassistant.dto.request.SignupRequest;
import com.farmerassistant.dto.response.AuthResponse;
import com.farmerassistant.exception.DuplicateResourceException;
import com.farmerassistant.model.User;
import com.farmerassistant.repository.*;
import com.farmerassistant.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FarmerRepository farmerRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private OtpTokenRepository otpTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private com.farmerassistant.config.EmailService emailService;

    @Mock
    private RateLimitingService rateLimitingService;

    @InjectMocks
    private AuthService authService;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    public void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setEmail("test@farmer.com");
        signupRequest.setPassword("Test@1234");
        signupRequest.setFirstName("Ramesh");
        signupRequest.setLastName("Kumar");
        signupRequest.setPhone("9876543210");
        signupRequest.setRole(User.Role.FARMER);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@farmer.com");
        loginRequest.setPassword("Test@1234");

        testUser = User.builder()
                .id(1L)
                .email("test@farmer.com")
                .passwordHash("hashedPassword")
                .firstName("Ramesh")
                .lastName("Kumar")
                .phone("9876543210")
                .role(User.Role.FARMER)
                .isActive(true)
                .isEmailVerified(false)
                .build();
    }

    @Test
    public void signup_success() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateAccessToken(anyString(), anyString())).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refreshToken");

        AuthResponse response = authService.signup(signupRequest);

        assertNotNull(response);
        assertEquals("test@farmer.com", response.getUser().getEmail());
        assertEquals("accessToken", response.getAccessToken());
        verify(emailService, times(1)).sendOtpEmail(anyString(), anyString(), anyString());
        verify(emailService, times(1)).sendWelcomeEmail(anyString(), anyString());
    }

    @Test
    public void signup_duplicateEmail_throwsException() {
        when(userRepository.existsByEmail(any())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.signup(signupRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    public void login_success() {
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateAccessToken(any(Authentication.class))).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refreshToken");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        verify(rateLimitingService, times(1)).resetLoginAttempts(anyString());
    }

    @Test
    public void login_blockedAccount_throwsException() {
        when(rateLimitingService.isLoginBlocked(any())).thenReturn(true);

        assertThrows(com.farmerassistant.exception.AccessForbiddenException.class, () -> authService.login(loginRequest));
    }

    @Test
    public void login_badCredentials_recordsFailedAttempt() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
        verify(rateLimitingService, times(1)).recordFailedLogin(anyString());
    }
}
