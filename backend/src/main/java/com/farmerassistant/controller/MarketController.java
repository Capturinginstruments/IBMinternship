package com.farmerassistant.controller;

import com.farmerassistant.dto.response.ApiResponse;
import com.farmerassistant.dto.response.MarketPriceResponse;
import com.farmerassistant.service.MarketPriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
@Tag(name = "Market Prices", description = "Agricultural market price APIs")
public class MarketController {

    private final MarketPriceService marketService;

    @GetMapping("/prices")
    @Operation(summary = "Get market prices with optional filters")
    public ResponseEntity<ApiResponse<List<MarketPriceResponse>>> getPrices(
            @RequestParam(required = false) String commodity,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String district) {
        return ResponseEntity.ok(ApiResponse.success(
                marketService.fetchPrices(commodity, state, district)));
    }

    @GetMapping("/trend")
    @Operation(summary = "Get price trend for a commodity")
    public ResponseEntity<ApiResponse<List<MarketPriceResponse>>> getTrend(
            @RequestParam String commodity,
            @RequestParam(required = false, defaultValue = "Maharashtra") String state,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(ApiResponse.success(
                marketService.getPriceTrend(commodity, state, days)));
    }

    @GetMapping("/commodities")
    @Operation(summary = "Get list of all available commodities")
    public ResponseEntity<ApiResponse<List<String>>> getCommodities() {
        return ResponseEntity.ok(ApiResponse.success(marketService.getAllCommodities()));
    }

    @GetMapping("/states")
    @Operation(summary = "Get list of all available states")
    public ResponseEntity<ApiResponse<List<String>>> getStates() {
        return ResponseEntity.ok(ApiResponse.success(marketService.getAllStates()));
    }

    @GetMapping("/advice")
    @Operation(summary = "Get AI advice on whether to sell a commodity now or wait")
    public ResponseEntity<ApiResponse<Map<String, String>>> getSellAdvice(
            @RequestParam String commodity,
            @RequestParam(required = false, defaultValue = "Maharashtra") String state) {
        String advice = marketService.generateSellAdvice(commodity, state);
        return ResponseEntity.ok(ApiResponse.success(Map.of("advice", advice)));
    }
}
