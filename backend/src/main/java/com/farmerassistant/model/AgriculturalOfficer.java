package com.farmerassistant.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "agricultural_officers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgriculturalOfficer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 200)
    private String designation;

    @Column(length = 100)
    private String district;

    @Column(length = 100)
    private String state;

    @Column(name = "employee_id", unique = true, length = 50)
    private String employeeId;

    @Column(length = 200)
    private String department;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
