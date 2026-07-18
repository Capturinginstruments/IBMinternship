package com.farmerassistant.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendOtpEmail(String to, String otp, String purpose) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("AI Farmer Assistant <" + fromEmail + ">");
            message.setTo(to);

            if ("EMAIL_VERIFY".equals(purpose)) {
                message.setSubject("Verify your AI Farmer Assistant account");
                message.setText(
                    "Dear Farmer,\n\n" +
                    "Thank you for registering with AI Farmer Assistant!\n\n" +
                    "Your email verification OTP is: " + otp + "\n\n" +
                    "This OTP is valid for 15 minutes.\n\n" +
                    "If you did not register, please ignore this email.\n\n" +
                    "Jai Kisan!\n" +
                    "AI Farmer Assistant Team"
                );
            } else {
                message.setSubject("Password Reset OTP - AI Farmer Assistant");
                message.setText(
                    "Dear User,\n\n" +
                    "We received a request to reset your password.\n\n" +
                    "Your password reset OTP is: " + otp + "\n\n" +
                    "This OTP is valid for 15 minutes.\n\n" +
                    "If you did not request a password reset, please ignore this email.\n\n" +
                    "AI Farmer Assistant Team"
                );
            }

            mailSender.send(message);
            log.info("OTP email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendWelcomeEmail(String to, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("AI Farmer Assistant <" + fromEmail + ">");
            message.setTo(to);
            message.setSubject("Welcome to AI Farmer Assistant!");
            message.setText(
                "Dear " + firstName + ",\n\n" +
                "Welcome to AI Farmer Assistant - Your Digital Farming Companion!\n\n" +
                "You can now access:\n" +
                "• AI-powered Crop Recommendations\n" +
                "• Real-time Weather Updates & Farming Advice\n" +
                "• Plant Disease Detection\n" +
                "• Live Market Prices\n" +
                "• Government Agriculture Schemes\n" +
                "• 24/7 AI Farming Chatbot\n\n" +
                "Jai Kisan! Jai Bharat!\n" +
                "AI Farmer Assistant Team"
            );
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", to, e.getMessage());
        }
    }
}
