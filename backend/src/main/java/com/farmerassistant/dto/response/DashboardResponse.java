package com.farmerassistant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private WeatherSummary weather;
    private CropRecommendationResponse latestCropRecommendation;
    private long unresolvedDiseaseAlerts;
    private long unreadNotifications;
    private List<SchemeResponse> featuredSchemes;
    private List<ChatSessionSummary> recentChats;
    private MarketSummary marketSummary;
    private FarmerProfileSummary farmerProfile;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeatherSummary {
        private String city;
        private Double temperature;
        private String description;
        private String iconCode;
        private Integer humidity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketSummary {
        private String commodity;
        private BigDecimal modalPrice;
        private String priceUnit;
        private Double priceChangePercent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatSessionSummary {
        private String sessionId;
        private String lastMessage;
        private String lastUpdated;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FarmerProfileSummary {
        private String fullName;
        private String profileImageUrl;
        private String primaryCrop;
        private String district;
        private String state;
    }
}
