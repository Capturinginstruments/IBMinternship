package com.farmerassistant.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "disease_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiseaseReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "image_s3_key", length = 500)
    private String imageS3Key;

    @Column(name = "crop_type", length = 100)
    private String cropType;

    @Column(name = "disease_name", length = 200)
    private String diseaseName;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Lob
    @Column(name = "treatment")
    private String treatment;

    @Column(name = "medicine")
    private String medicine;

    @Lob
    @Column(name = "prevention")
    private String prevention;

    @Lob
    @Column(name = "gemini_explanation")
    private String geminiExplanation;

    @Column(name = "is_resolved", nullable = false)
    @Builder.Default
    private boolean isResolved = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
