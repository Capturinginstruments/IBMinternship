package com.farmerassistant.service;

import com.farmerassistant.dto.response.WeatherResponse;
import com.farmerassistant.exception.ExternalServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private final RestTemplate restTemplate;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openweather.api.key}")
    private String apiKey;

    @Value("${openweather.api.base-url}")
    private String baseUrl;

    @Cacheable(value = "weather", key = "'coords-'+#lat+'-'+#lon")
    public WeatherResponse getWeatherByCoords(Double lat, Double lon) {
        try {
            String currentUrl = baseUrl + "/weather?lat=" + lat + "&lon=" + lon +
                    "&appid=" + apiKey + "&units=metric&lang=en";
            String forecastUrl = baseUrl + "/forecast?lat=" + lat + "&lon=" + lon +
                    "&appid=" + apiKey + "&units=metric&cnt=40";

            String currentJson = restTemplate.getForObject(currentUrl, String.class);
            String forecastJson = restTemplate.getForObject(forecastUrl, String.class);

            WeatherResponse response = parseCurrentWeather(currentJson);
            response.setForecast(parseForecast(forecastJson));
            response.setGeminiAdvice(generateFarmingAdvice(response));
            return response;
        } catch (Exception e) {
            log.error("Weather API error: {}", e.getMessage());
            throw new ExternalServiceException("Unable to fetch weather data: " + e.getMessage());
        }
    }

    @Cacheable(value = "weather", key = "'city-'+#city")
    public WeatherResponse getWeatherByCity(String city) {
        try {
            String currentUrl = baseUrl + "/weather?q=" + city + ",IN&appid=" + apiKey + "&units=metric";
            String currentJson = restTemplate.getForObject(currentUrl, String.class);
            JsonNode root = objectMapper.readTree(currentJson);
            double lat = root.path("coord").path("lat").asDouble();
            double lon = root.path("coord").path("lon").asDouble();
            return getWeatherByCoords(lat, lon);
        } catch (Exception e) {
            throw new ExternalServiceException("Unable to fetch weather for city: " + city);
        }
    }

    private WeatherResponse parseCurrentWeather(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        JsonNode main = root.path("main");
        JsonNode wind = root.path("wind");
        JsonNode weather = root.path("weather").get(0);
        JsonNode sys = root.path("sys");
        JsonNode coord = root.path("coord");
        JsonNode rain = root.path("rain");

        return WeatherResponse.builder()
                .city(root.path("name").asText())
                .country(sys.path("country").asText("IN"))
                .latitude(coord.path("lat").asDouble())
                .longitude(coord.path("lon").asDouble())
                .temperature(main.path("temp").asDouble())
                .feelsLike(main.path("feels_like").asDouble())
                .tempMin(main.path("temp_min").asDouble())
                .tempMax(main.path("temp_max").asDouble())
                .humidity(main.path("humidity").asInt())
                .pressure(main.path("pressure").asInt())
                .windSpeed(wind.path("speed").asDouble())
                .windDegree(wind.path("deg").asInt())
                .description(weather != null ? weather.path("description").asText() : "")
                .iconCode(weather != null ? weather.path("icon").asText() : "01d")
                .cloudiness(root.path("clouds").path("all").asInt())
                .rainfall1h(rain.path("1h").asDouble(0.0))
                .sunrise(sys.path("sunrise").asLong())
                .sunset(sys.path("sunset").asLong())
                .build();
    }

    private List<WeatherResponse.DayForecast> parseForecast(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        JsonNode list = root.path("list");

        Map<String, List<JsonNode>> dayMap = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (JsonNode item : list) {
            long dt = item.path("dt").asLong();
            String date = Instant.ofEpochSecond(dt)
                    .atZone(ZoneId.of("Asia/Kolkata"))
                    .toLocalDate().format(fmt);
            dayMap.computeIfAbsent(date, k -> new ArrayList<>()).add(item);
        }

        List<WeatherResponse.DayForecast> forecasts = new ArrayList<>();
        for (Map.Entry<String, List<JsonNode>> entry : dayMap.entrySet()) {
            if (forecasts.size() >= 5) break;
            String date = entry.getKey();
            List<JsonNode> items = entry.getValue();

            DoubleSummaryStatistics temps = items.stream()
                    .mapToDouble(n -> n.path("main").path("temp").asDouble()).summaryStatistics();
            double avgHumidity = items.stream()
                    .mapToInt(n -> n.path("main").path("humidity").asInt()).average().orElse(0);
            double avgWind = items.stream()
                    .mapToDouble(n -> n.path("wind").path("speed").asDouble()).average().orElse(0);
            double maxRain = items.stream()
                    .mapToDouble(n -> n.path("pop").asDouble(0)).max().orElse(0);

            JsonNode midItem = items.get(items.size() / 2);
            JsonNode weather = midItem.path("weather").get(0);

            LocalDate localDate = LocalDate.parse(date, fmt);
            String dayName = localDate.getDayOfWeek().getDisplayName(
                    java.time.format.TextStyle.SHORT, Locale.ENGLISH);

            forecasts.add(WeatherResponse.DayForecast.builder()
                    .date(date).dayName(dayName)
                    .tempMin(Math.round(temps.getMin() * 10.0) / 10.0)
                    .tempMax(Math.round(temps.getMax() * 10.0) / 10.0)
                    .tempAvg(Math.round(temps.getAverage() * 10.0) / 10.0)
                    .humidity((int) avgHumidity)
                    .windSpeed(Math.round(avgWind * 10.0) / 10.0)
                    .description(weather != null ? weather.path("description").asText() : "")
                    .iconCode(weather != null ? weather.path("icon").asText() : "01d")
                    .rainProbability(maxRain * 100)
                    .build());
        }
        return forecasts;
    }

    private String generateFarmingAdvice(WeatherResponse weather) {
        try {
            String prompt = String.format("""
                You are an expert agricultural advisor for Indian farmers. 
                Based on the following weather conditions, provide specific, practical farming advice in 3-4 sentences.
                Focus on what farmers should do TODAY given these conditions.
                
                Location: %s, India
                Temperature: %.1f°C (feels like %.1f°C)
                Humidity: %d%%
                Wind Speed: %.1f m/s
                Description: %s
                Cloudiness: %d%%
                Rainfall (last 1h): %.1f mm
                
                Provide advice on: irrigation needs, field operations suitability, pest/disease risk, 
                crop protection measures, and any urgent actions needed.
                Keep the response concise and practical for a typical Indian farmer.
                """,
                    weather.getCity(), weather.getTemperature(), weather.getFeelsLike(),
                    weather.getHumidity(), weather.getWindSpeed(), weather.getDescription(),
                    weather.getCloudiness(), weather.getRainfall1h() != null ? weather.getRainfall1h() : 0.0);

            return geminiService.generateContent(prompt);
        } catch (Exception e) {
            log.warn("Could not generate farming advice: {}", e.getMessage());
            return "Weather data loaded. Please consult your local agricultural officer for specific farming advice.";
        }
    }
}
