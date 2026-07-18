package com.farmerassistant.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    // ─── LOGIN RATE LIMITING ──────────────────────────────────────────────────
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOGIN_LOCK_MINUTES = 15;
    private final Map<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();

    // ─── OTP BRUTE-FORCE PROTECTION ──────────────────────────────────────────
    private static final int MAX_OTP_ATTEMPTS = 3;
    private static final int OTP_LOCK_MINUTES = 10;
    private final Map<String, OtpAttempt> otpAttempts = new ConcurrentHashMap<>();

    // ─── JWT BLACKLIST ────────────────────────────────────────────────────────
    private final Map<String, LocalDateTime> jwtBlacklist = new ConcurrentHashMap<>();

    // ─── LOGIN ATTEMPT LOGIC ─────────────────────────────────────────────────
    public boolean isLoginBlocked(String key) {
        LoginAttempt attempt = loginAttempts.get(key);
        if (attempt == null) return false;
        
        if (attempt.attempts >= MAX_LOGIN_ATTEMPTS) {
            if (LocalDateTime.now().isBefore(attempt.lockoutTime)) {
                return true;
            } else {
                loginAttempts.remove(key); // Lock expired
            }
        }
        return false;
    }

    public void recordFailedLogin(String key) {
        loginAttempts.compute(key, (k, v) -> {
            if (v == null) {
                return new LoginAttempt(1, LocalDateTime.now().plusMinutes(LOGIN_LOCK_MINUTES));
            }
            v.attempts++;
            if (v.attempts >= MAX_LOGIN_ATTEMPTS) {
                v.lockoutTime = LocalDateTime.now().plusMinutes(LOGIN_LOCK_MINUTES);
            }
            return v;
        });
    }

    public void resetLoginAttempts(String key) {
        loginAttempts.remove(key);
    }

    // ─── OTP ATTEMPT LOGIC ────────────────────────────────────────────────────
    public boolean isOtpBlocked(String email) {
        OtpAttempt attempt = otpAttempts.get(email);
        if (attempt == null) return false;

        if (attempt.attempts >= MAX_OTP_ATTEMPTS) {
            if (LocalDateTime.now().isBefore(attempt.lockoutTime)) {
                return true;
            } else {
                otpAttempts.remove(email); // Lock expired
            }
        }
        return false;
    }

    public void recordFailedOtp(String email) {
        otpAttempts.compute(email, (k, v) -> {
            if (v == null) {
                return new OtpAttempt(1, LocalDateTime.now().plusMinutes(OTP_LOCK_MINUTES));
            }
            v.attempts++;
            if (v.attempts >= MAX_OTP_ATTEMPTS) {
                v.lockoutTime = LocalDateTime.now().plusMinutes(OTP_LOCK_MINUTES);
            }
            return v;
        });
    }

    public void resetOtpAttempts(String email) {
        otpAttempts.remove(email);
    }

    // ─── JWT BLACKLIST LOGIC ──────────────────────────────────────────────────
    public void blacklistJwt(String token, long expiryTimeMs) {
        LocalDateTime expiry = LocalDateTime.now().plusSeconds(expiryTimeMs / 1000);
        jwtBlacklist.put(token, expiry);
        
        // Clean expired tokens from map periodically in a lazy manner
        jwtBlacklist.entrySet().removeIf(entry -> LocalDateTime.now().isAfter(entry.getValue()));
    }

    public boolean isJwtBlacklisted(String token) {
        LocalDateTime expiry = jwtBlacklist.get(token);
        if (expiry == null) return false;
        
        if (LocalDateTime.now().isAfter(expiry)) {
            jwtBlacklist.remove(token);
            return false;
        }
        return true;
    }

    // Helper classes
    private static class LoginAttempt {
        int attempts;
        LocalDateTime lockoutTime;

        LoginAttempt(int attempts, LocalDateTime lockoutTime) {
            this.attempts = attempts;
            this.lockoutTime = lockoutTime;
        }
    }

    private static class OtpAttempt {
        int attempts;
        LocalDateTime lockoutTime;

        OtpAttempt(int attempts, LocalDateTime lockoutTime) {
            this.attempts = attempts;
            this.lockoutTime = lockoutTime;
        }
    }
}
