package com.farmerassistant.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "crop_recommendations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String district;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Season season;

    @Column(name = "soil_type", length = 50)
    private String soilType;

    private Double nitrogen;
    private Double phosphorus;
    private Double potassium;
    private Double temperature;
    private Double humidity;
    private Double rainfall;

    @Column(name = "ph_level")
    private Double phLevel;

    @Column(name = "recommended_crop", length = 100)
    private String recommendedCrop;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "expected_yield", length = 100)
    private String expectedYield;

    @Column(name = "profit_estimate", length = 100)
    private String profitEstimate;

    @Column(name = "water_requirement", length = 100)
    private String waterRequirement;

    @Lob
    @Column(name = "fertilizer_advice")
    private String fertilizerAdvice;

    @Lob
    @Column(name = "gemini_explanation")
    private String geminiExplanation;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum Season {
        SUMMER, WINTER, RAINY
    }
}
