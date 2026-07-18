package com.farmerassistant.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_prices",
    uniqueConstraints = @UniqueConstraint(columnNames = {"commodity","market_name","trade_date"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String commodity;

    @Column(name = "market_name", length = 200)
    private String marketName;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String district;

    @Column(name = "min_price", precision = 10, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 10, scale = 2)
    private BigDecimal maxPrice;

    @Column(name = "modal_price", precision = 10, scale = 2)
    private BigDecimal modalPrice;

    @Column(name = "price_unit", length = 50)
    @Builder.Default
    private String priceUnit = "per quintal";

    @Column(name = "trade_date")
    private LocalDate tradeDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
