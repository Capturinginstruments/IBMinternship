package com.farmerassistant.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "farmers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Farmer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String district;

    @Column(length = 100)
    private String village;

    @Column(name = "land_acres", precision = 10, scale = 2)
    private BigDecimal landAcres;

    @Enumerated(EnumType.STRING)
    @Column(name = "soil_type", length = 20)
    private SoilType soilType;

    @Column(name = "primary_crop", length = 100)
    private String primaryCrop;

    @Column(name = "secondary_crop", length = 100)
    private String secondaryCrop;

    @Column(name = "water_source", length = 100)
    private String waterSource;

    @Column(name = "aadhaar_masked", length = 20)
    private String aadhaarMasked;

    @Column(name = "kcc_number", length = 50)
    private String kccNumber;

    private Double latitude;
    private Double longitude;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum SoilType {
        CLAY, SANDY, LOAMY, SILT, CHALKY, PEAT
    }
}
