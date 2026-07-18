package com.farmerassistant.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "government_schemes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernmentScheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "eligibility", columnDefinition = "TEXT")
    private String eligibility;

    @Column(name = "benefits", columnDefinition = "TEXT")
    private String benefits;

    @Column(name = "documents_required", columnDefinition = "TEXT")
    private String documentsRequired;

    @Column(name = "official_url", length = 500)
    private String officialUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SchemeCategory category;

    @Column(name = "applicable_states", columnDefinition = "TEXT")
    private String applicableStates;

    @Column(name = "applicable_crops", columnDefinition = "TEXT")
    private String applicableCrops;

    @Column(name = "min_land_acres", precision = 10, scale = 2)
    private BigDecimal minLandAcres;

    @Column(name = "max_land_acres", precision = 10, scale = 2)
    private BigDecimal maxLandAcres;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum SchemeCategory {
        SUBSIDY, LOAN, INSURANCE, TRAINING, EQUIPMENT, SEED, FERTILIZER
    }
}
