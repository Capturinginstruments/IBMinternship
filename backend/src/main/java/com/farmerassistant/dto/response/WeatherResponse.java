package com.farmerassistant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {
    private String city;
    private String country;
    private Double latitude;
    private Double longitude;
    private Double temperature;
    private Double feelsLike;
    private Double tempMin;
    private Double tempMax;
    private Integer humidity;
    private Integer pressure;
    private Double windSpeed;
    private Integer windDegree;
    private Double uvIndex;
    private String description;
    private String iconCode;
    private Integer cloudiness;
    private Double rainProbability;
    private Double rainfall1h;
    private Long sunrise;
    private Long sunset;
    private List<DayForecast> forecast;
    private String geminiAdvice;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayForecast {
        private String date;
        private String dayName;
        private Double tempMin;
        private Double tempMax;
        private Double tempAvg;
        private Integer humidity;
        private Double windSpeed;
        private String description;
        private String iconCode;
        private Double rainProbability;
    }
}
