package com.farmerassistant.service;

import com.farmerassistant.dto.response.MarketPriceResponse;
import com.farmerassistant.model.MarketPrice;
import com.farmerassistant.repository.MarketPriceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketPriceService {

    private final MarketPriceRepository marketPriceRepository;
    private final GeminiService geminiService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${datagovin.api.key}")
    private String dataGovApiKey;

    @Value("${datagovin.api.base-url}")
    private String dataGovBaseUrl;

    private static final String AGMARKNET_RESOURCE_ID = "9ef84268-d588-465a-a308-a864a43d0070";

    @Cacheable(value = "marketPrices", key = "#commodity+'-'+#state+'-'+#district")
    @Transactional
    public List<MarketPriceResponse> fetchPrices(String commodity, String state, String district) {
        try {
            StringBuilder url = new StringBuilder(dataGovBaseUrl)
                    .append("/").append(AGMARKNET_RESOURCE_ID)
                    .append("?api-key=").append(dataGovApiKey)
                    .append("&format=json&limit=50");

            if (commodity != null && !commodity.isEmpty())
                url.append("&filters[commodity]=").append(commodity);
            if (state != null && !state.isEmpty())
                url.append("&filters[state]=").append(state);
            if (district != null && !district.isEmpty())
                url.append("&filters[district]=").append(district);

            String responseJson = restTemplate.getForObject(url.toString(), String.class);
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode records = root.path("records");

            List<MarketPriceResponse> responses = new ArrayList<>();
            if (records.isArray()) {
                for (JsonNode record : records) {
                    MarketPriceResponse resp = mapRecord(record);
                    responses.add(resp);
                    saveOrUpdateMarketPrice(record);
                }
            }
            return responses;
        } catch (Exception e) {
            log.warn("data.gov.in API failed, falling back to DB: {}", e.getMessage());
            return getFallbackPrices(commodity, state, district);
        }
    }

    @Transactional(readOnly = true)
    public List<MarketPriceResponse> getPriceTrend(String commodity, String state, int days) {
        LocalDate fromDate = LocalDate.now().minusDays(days);
        String searchCommodity = commodity != null ? commodity.toLowerCase().trim() : null;
        String searchState = state != null ? state.toLowerCase().trim() : null;
        List<MarketPrice> prices = marketPriceRepository.findPriceTrend(searchCommodity, searchState, fromDate);
        return prices.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getAllCommodities() {
        return marketPriceRepository.findDistinctCommodities();
    }

    @Transactional(readOnly = true)
    public List<String> getAllStates() {
        return marketPriceRepository.findDistinctStates();
    }

    public String generateSellAdvice(String commodity, String state) {
        String searchCommodity = commodity != null ? commodity.toLowerCase().trim() : "";
        String searchState = state != null ? state.toLowerCase().trim() : "";
        List<MarketPrice> recentPrices = marketPriceRepository
                .findByCommodityAndState(searchCommodity, searchState)
                .stream().limit(7).collect(Collectors.toList());

        if (recentPrices.isEmpty()) {
            return "Market data not available for this commodity. Please check local mandi prices.";
        }

        StringBuilder priceData = new StringBuilder();
        for (MarketPrice p : recentPrices) {
            priceData.append(String.format("Date: %s, Min: ₹%.0f, Max: ₹%.0f, Modal: ₹%.0f/quintal\n",
                    p.getTradeDate(), p.getMinPrice(), p.getMaxPrice(), p.getModalPrice()));
        }

        String prompt = String.format("""
            You are an expert agricultural market analyst for Indian farmers.
            Analyze the following %s price data from %s and advise whether the farmer should sell now or wait.
            
            Price Trend (last 7 days):
            %s
            
            Consider: price trend direction, seasonal patterns, MSP comparison, storage costs, and market demand.
            Provide a clear recommendation in 3-4 sentences. Start with "SELL NOW" or "WAIT TO SELL" in bold.
            """, commodity, state, priceData);

        try {
            return geminiService.generateContent(prompt);
        } catch (Exception e) {
            return "Unable to generate AI advice. Based on current data, modal price is ₹" +
                    recentPrices.get(0).getModalPrice() + "/quintal.";
        }
    }

    private List<MarketPriceResponse> getFallbackPrices(String commodity, String state, String district) {
        String searchCommodity = commodity != null && !commodity.isEmpty() ? "%" + commodity.toLowerCase().trim() + "%" : null;
        String searchState = state != null && !state.isEmpty() ? state.toLowerCase().trim() : null;
        String searchDistrict = district != null && !district.isEmpty() ? district.toLowerCase().trim() : null;
        List<MarketPrice> prices = marketPriceRepository.findByFilters(searchCommodity, searchState, searchDistrict);
        return prices.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private MarketPriceResponse mapRecord(JsonNode record) {
        return MarketPriceResponse.builder()
                .commodity(record.path("commodity").asText())
                .marketName(record.path("market").asText())
                .state(record.path("state").asText())
                .district(record.path("district").asText())
                .minPrice(safeDecimal(record.path("min_price").asText("0")))
                .maxPrice(safeDecimal(record.path("max_price").asText("0")))
                .modalPrice(safeDecimal(record.path("modal_price").asText("0")))
                .priceUnit("per quintal")
                .tradeDate(parseDate(record.path("arrival_date").asText()))
                .build();
    }

    private void saveOrUpdateMarketPrice(JsonNode record) {
        try {
            MarketPrice price = MarketPrice.builder()
                    .commodity(record.path("commodity").asText())
                    .marketName(record.path("market").asText())
                    .state(record.path("state").asText())
                    .district(record.path("district").asText())
                    .minPrice(safeDecimal(record.path("min_price").asText("0")))
                    .maxPrice(safeDecimal(record.path("max_price").asText("0")))
                    .modalPrice(safeDecimal(record.path("modal_price").asText("0")))
                    .tradeDate(parseDate(record.path("arrival_date").asText()))
                    .build();
            marketPriceRepository.save(price);
        } catch (Exception e) {
            log.debug("Could not save market price record: {}", e.getMessage());
        }
    }

    private MarketPriceResponse toResponse(MarketPrice mp) {
        return MarketPriceResponse.builder()
                .id(mp.getId()).commodity(mp.getCommodity()).marketName(mp.getMarketName())
                .state(mp.getState()).district(mp.getDistrict())
                .minPrice(mp.getMinPrice()).maxPrice(mp.getMaxPrice()).modalPrice(mp.getModalPrice())
                .priceUnit(mp.getPriceUnit()).tradeDate(mp.getTradeDate()).build();
    }

    private BigDecimal safeDecimal(String val) {
        try { return new BigDecimal(val.trim()); }
        catch (Exception e) { return BigDecimal.ZERO; }
    }

    private LocalDate parseDate(String dateStr) {
        try {
            if (dateStr.contains("/")) {
                String[] parts = dateStr.split("/");
                if (parts.length == 3) return LocalDate.of(
                        Integer.parseInt(parts[2]), Integer.parseInt(parts[1]), Integer.parseInt(parts[0]));
            }
            return LocalDate.parse(dateStr);
        } catch (Exception e) { return LocalDate.now(); }
    }
}
