package com.farmerassistant.service;

import com.farmerassistant.dto.request.CropRecommendationRequest;
import com.farmerassistant.dto.response.CropRecommendationResponse;
import com.farmerassistant.exception.ExternalServiceException;
import com.farmerassistant.model.CropRecommendation;
import com.farmerassistant.model.User;
import com.farmerassistant.repository.CropRecommendationRepository;
import com.farmerassistant.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CropRecommendationService {

    private final CropRecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CropRecommendationResponse recommend(CropRecommendationRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.farmerassistant.exception.ResourceNotFoundException("User not found with id: " + userId));

        String prompt = buildCropRecommendationPrompt(request);
        String geminiResponse = geminiService.generateContent(prompt);
        String jsonStr = geminiService.extractJsonFromResponse(geminiResponse);

        CropRecommendation recommendation = parseCropRecommendation(jsonStr, request, user);
        recommendation = recommendationRepository.save(recommendation);

        return toResponse(recommendation);
    }

    @Transactional(readOnly = true)
    public Page<CropRecommendationResponse> getHistory(Long userId, Pageable pageable) {
        return recommendationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CropRecommendationResponse getById(Long id, Long userId) {
        CropRecommendation rec = recommendationRepository.findById(id)
                .orElseThrow(() -> new com.farmerassistant.exception.ResourceNotFoundException("Recommendation not found with id: " + id));
        if (!rec.getUser().getId().equals(userId)) {
            throw new com.farmerassistant.exception.AccessForbiddenException("Access denied: You do not own this recommendation");
        }
        return toResponse(rec);
    }

    private String buildCropRecommendationPrompt(CropRecommendationRequest req) {
        return String.format("""
            You are an expert Indian agricultural scientist. Based on the following soil and climate parameters, 
            recommend the BEST SINGLE crop to grow. Respond ONLY with a valid JSON object (no markdown, no explanation outside JSON).
            
            Farm Parameters:
            - State: %s
            - District: %s
            - Season: %s
            - Soil Type: %s
            - Nitrogen (N): %.1f kg/ha
            - Phosphorus (P): %.1f kg/ha
            - Potassium (K): %.1f kg/ha
            - Temperature: %.1f°C
            - Humidity: %.1f%%
            - Rainfall: %.1f mm
            - Soil pH: %.1f
            
            Respond with this exact JSON structure:
            {
              "recommendedCrop": "crop name in English",
              "confidenceScore": 85.5,
              "expectedYield": "25-30 quintals/hectare",
              "profitEstimate": "₹45,000-55,000/hectare",
              "waterRequirement": "450-600 mm per season",
              "fertilizerAdvice": "Apply 120 kg Urea, 80 kg DAP, 40 kg MOP per hectare. Top dress with 60 kg Urea at flowering stage.",
              "explanation": "Detailed explanation in 3-4 sentences why this crop is ideal for these specific conditions including soil suitability, climate match, and market prospects for Indian farmers."
            }
            """,
                req.getState(), req.getDistrict(), req.getSeason(),
                req.getSoilType(), req.getNitrogen(), req.getPhosphorus(), req.getPotassium(),
                req.getTemperature(), req.getHumidity(), req.getRainfall(), req.getPhLevel());
    }

    private CropRecommendation parseCropRecommendation(String json, CropRecommendationRequest req, User user) {
        try {
            JsonNode node = objectMapper.readTree(json);
            return CropRecommendation.builder()
                    .user(user)
                    .state(req.getState())
                    .district(req.getDistrict())
                    .season(req.getSeason())
                    .soilType(req.getSoilType())
                    .nitrogen(req.getNitrogen())
                    .phosphorus(req.getPhosphorus())
                    .potassium(req.getPotassium())
                    .temperature(req.getTemperature())
                    .humidity(req.getHumidity())
                    .rainfall(req.getRainfall())
                    .phLevel(req.getPhLevel())
                    .recommendedCrop(node.path("recommendedCrop").asText("Unknown"))
                    .confidenceScore(node.path("confidenceScore").asDouble(75.0))
                    .expectedYield(node.path("expectedYield").asText())
                    .profitEstimate(node.path("profitEstimate").asText())
                    .waterRequirement(node.path("waterRequirement").asText())
                    .fertilizerAdvice(node.path("fertilizerAdvice").asText())
                    .geminiExplanation(node.path("explanation").asText())
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse Gemini crop recommendation: {}", e.getMessage());
            return CropRecommendation.builder()
                    .user(user).state(req.getState()).district(req.getDistrict())
                    .season(req.getSeason()).soilType(req.getSoilType())
                    .nitrogen(req.getNitrogen()).phosphorus(req.getPhosphorus())
                    .potassium(req.getPotassium()).temperature(req.getTemperature())
                    .humidity(req.getHumidity()).rainfall(req.getRainfall())
                    .phLevel(req.getPhLevel()).recommendedCrop("Unable to determine")
                    .confidenceScore(0.0).geminiExplanation(json).build();
        }
    }

    private CropRecommendationResponse toResponse(CropRecommendation rec) {
        return CropRecommendationResponse.builder()
                .id(rec.getId()).recommendedCrop(rec.getRecommendedCrop())
                .confidenceScore(rec.getConfidenceScore()).expectedYield(rec.getExpectedYield())
                .profitEstimate(rec.getProfitEstimate()).waterRequirement(rec.getWaterRequirement())
                .fertilizerAdvice(rec.getFertilizerAdvice()).geminiExplanation(rec.getGeminiExplanation())
                .season(rec.getSeason()).soilType(rec.getSoilType()).state(rec.getState())
                .district(rec.getDistrict()).nitrogen(rec.getNitrogen()).phosphorus(rec.getPhosphorus())
                .potassium(rec.getPotassium()).temperature(rec.getTemperature())
                .humidity(rec.getHumidity()).rainfall(rec.getRainfall()).phLevel(rec.getPhLevel())
                .createdAt(rec.getCreatedAt()).build();
    }
}
