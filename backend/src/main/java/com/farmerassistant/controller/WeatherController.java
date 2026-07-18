package com.farmerassistant.controller;

import com.farmerassistant.dto.response.ApiResponse;
import com.farmerassistant.dto.response.WeatherResponse;
import com.farmerassistant.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@Tag(name = "Weather", description = "Weather data with farming advice APIs")
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping
    @Operation(summary = "Get weather by GPS coordinates")
    public ResponseEntity<ApiResponse<WeatherResponse>> getWeatherByCoords(
            @RequestParam Double lat,
            @RequestParam Double lon) {
        WeatherResponse response = weatherService.getWeatherByCoords(lat, lon);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/city")
    @Operation(summary = "Get weather by city name")
    public ResponseEntity<ApiResponse<WeatherResponse>> getWeatherByCity(
            @RequestParam String city) {
        WeatherResponse response = weatherService.getWeatherByCity(city);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
