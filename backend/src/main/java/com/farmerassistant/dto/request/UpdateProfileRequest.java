package com.farmerassistant.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateProfileRequest {
    @Size(min = 2, max = 50, message = "First name must be 2-50 characters")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be 2-50 characters")
    private String lastName;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Please provide a valid Indian mobile number")
    private String phone;

    private String state;
    private String district;
    private String village;
    private BigDecimal landAcres;
    private String soilType;
    private String primaryCrop;
    private String secondaryCrop;
    private String waterSource;
    private Double latitude;
    private Double longitude;
}
