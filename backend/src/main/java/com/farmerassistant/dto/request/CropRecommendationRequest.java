package com.farmerassistant.dto.request;

import com.farmerassistant.model.CropRecommendation;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CropRecommendationRequest {

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "District is required")
    private String district;

    @NotNull(message = "Season is required")
    private CropRecommendation.Season season;

    @NotBlank(message = "Soil type is required")
    private String soilType;

    @NotNull(message = "Nitrogen value is required")
    @Min(value = 0) @Max(value = 200)
    private Double nitrogen;

    @NotNull(message = "Phosphorus value is required")
    @Min(value = 0) @Max(value = 200)
    private Double phosphorus;

    @NotNull(message = "Potassium value is required")
    @Min(value = 0) @Max(value = 200)
    private Double potassium;

    @NotNull(message = "Temperature is required")
    @Min(value = -10) @Max(value = 55)
    private Double temperature;

    @NotNull(message = "Humidity is required")
    @Min(value = 0) @Max(value = 100)
    private Double humidity;

    @NotNull(message = "Rainfall is required")
    @Min(value = 0) @Max(value = 5000)
    private Double rainfall;

    @NotNull(message = "pH level is required")
    @DecimalMin(value = "0.0") @DecimalMax(value = "14.0")
    private Double phLevel;
}
