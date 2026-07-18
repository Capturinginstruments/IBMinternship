package com.farmerassistant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiseaseReportResponse {
    private Long id;
    private String imageUrl;
    private String cropType;
    private String diseaseName;
    private Double confidenceScore;
    private String treatment;
    private String medicine;
    private String prevention;
    private String geminiExplanation;
    private boolean isResolved;
    private LocalDateTime createdAt;
}
