package com.farmerassistant.dto.request;

import com.farmerassistant.model.User;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be 2-50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be 2-50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
             message = "Password must be at least 8 characters and contain uppercase, lowercase, and a digit")
    private String password;

    @Pattern(regexp = "^(|[6-9]\\d{9})$", message = "Please provide a valid Indian mobile number")
    private String phone;

    private User.Role role = User.Role.FARMER;
}
