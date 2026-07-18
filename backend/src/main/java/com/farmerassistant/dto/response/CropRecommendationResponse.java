package com.farmerassistant.dto.response;

import com.farmerassistant.model.CropRecommendation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropRecommendationResponse {
    private Long id;
    private String recommendedCrop;
    private Double confidenceScore;
    private String expectedYield;
    private String profitEstimate;
    private String waterRequirement;
    private String fertilizerAdvice;
    private String geminiExplanation;
    private CropRecommendation.Season season;
    private String soilType;
    private String state;
    private String district;
    private Double nitrogen;
    private Double phosphorus;
    private Double potassium;
    private Double temperature;
    private Double humidity;
    private Double rainfall;
    private Double phLevel;
    private LocalDateTime createdAt;
}
