package com.farmerassistant.dto.response;

import com.farmerassistant.model.GovernmentScheme;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchemeResponse {
    private Long id;
    private String title;
    private String description;
    private String eligibility;
    private String benefits;
    private String documentsRequired;
    private String officialUrl;
    private GovernmentScheme.SchemeCategory category;
    private String applicableStates;
    private String applicableCrops;
    private BigDecimal minLandAcres;
    private BigDecimal maxLandAcres;
    private LocalDate deadline;
    private boolean isActive;
    private boolean isBookmarked;
    private LocalDateTime createdAt;
}
