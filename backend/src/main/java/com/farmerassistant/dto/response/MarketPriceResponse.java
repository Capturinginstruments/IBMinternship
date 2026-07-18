package com.farmerassistant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketPriceResponse {
    private Long id;
    private String commodity;
    private String marketName;
    private String state;
    private String district;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal modalPrice;
    private String priceUnit;
    private LocalDate tradeDate;
    private String geminiAdvice;
    private Double priceChangePercent;
}
