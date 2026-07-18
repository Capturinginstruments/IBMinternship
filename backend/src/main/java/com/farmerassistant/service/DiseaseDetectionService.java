package com.farmerassistant.service;

import com.farmerassistant.dto.response.DiseaseReportResponse;
import com.farmerassistant.exception.ExternalServiceException;
import com.farmerassistant.model.DiseaseReport;
import com.farmerassistant.model.User;
import com.farmerassistant.repository.DiseaseReportRepository;
import com.farmerassistant.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DiseaseDetectionService {

    private final S3Service s3Service;
    private final GeminiService geminiService;
    private final DiseaseReportRepository diseaseReportRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${huggingface.api.key}")
    private String hfApiKey;

    @Value("${huggingface.api.base-url}")
    private String hfBaseUrl;

    @Value("${huggingface.api.plant-disease-model}")
    private String plantDiseaseModel;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp");

    public DiseaseReportResponse detect(MultipartFile image, String cropType, Long userId) throws Exception {
        validateImage(image);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.farmerassistant.exception.ResourceNotFoundException("User not found with id: " + userId));

        String imageUrl = s3Service.uploadFile(image, "disease-images");
        String base64Image = Base64.getEncoder().encodeToString(image.getBytes());
        String mimeType = image.getContentType();

        HFPrediction prediction = classifyWithHuggingFace(image.getBytes());
        String diseaseName = formatDiseaseName(prediction.getLabel());
        Double confidence = prediction.getScore() * 100;

        String geminiPrompt = buildDiseasePrompt(diseaseName, cropType);
        String geminiResponse = geminiService.generateContentWithImage(geminiPrompt, base64Image, mimeType);
        String jsonStr = geminiService.extractJsonFromResponse(geminiResponse);

        DiseaseReport report = parseDiseaseReport(jsonStr, user, imageUrl, cropType, diseaseName, confidence);
        report = diseaseReportRepository.save(report);

        return toResponse(report);
    }

    @Transactional(readOnly = true)
    public Page<DiseaseReportResponse> getHistory(Long userId, Pageable pageable) {
        return diseaseReportRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    public DiseaseReportResponse markResolved(Long reportId, Long userId) {
        DiseaseReport report = diseaseReportRepository.findById(reportId)
                .orElseThrow(() -> new com.farmerassistant.exception.ResourceNotFoundException("Report not found with id: " + reportId));
        if (!report.getUser().getId().equals(userId)) {
            throw new com.farmerassistant.exception.AccessForbiddenException("Access denied: You do not own this disease report");
        }
        report.setResolved(true);
        return toResponse(diseaseReportRepository.save(report));
    }

    private void validateImage(MultipartFile image) {
        if (image.isEmpty()) throw new IllegalArgumentException("Image file is empty");
        if (image.getSize() > 10 * 1024 * 1024)
            throw new IllegalArgumentException("Image size must be less than 10MB");
        if (!ALLOWED_TYPES.contains(image.getContentType()))
            throw new IllegalArgumentException("Only JPEG, PNG, and WebP images are supported");
    }

    private HFPrediction classifyWithHuggingFace(byte[] imageBytes) {
        String url = hfBaseUrl + "/" + plantDiseaseModel;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + hfApiKey);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                HttpEntity<byte[]> entity = new HttpEntity<>(imageBytes, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

                JsonNode results = objectMapper.readTree(response.getBody());
                if (results.isArray() && results.size() > 0) {
                    JsonNode top = results.get(0);
                    return new HFPrediction(
                            top.path("label").asText("Unknown"),
                            top.path("score").asDouble(0.0));
                }
            } catch (Exception e) {
                if (attempt == 3) {
                    log.error("HuggingFace API failed after 3 attempts: {}", e.getMessage());
                    return new HFPrediction("Disease detection unavailable", 0.0);
                }
                try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
        return new HFPrediction("Unknown", 0.0);
    }

    private String formatDiseaseName(String label) {
        if (label == null || label.isEmpty()) return "Unknown Disease";
        return label.replace("___", " - ")
                .replace("_", " ")
                .replaceAll("([a-z])([A-Z])", "$1 $2");
    }

    private String buildDiseasePrompt(String diseaseName, String cropType) {
        return String.format("""
            Analyze this crop image. The AI model detected: "%s" on a %s plant.
            
            Provide a comprehensive response as a JSON object with these exact fields:
            {
              "diseaseName": "exact disease name",
              "treatment": "Step-by-step treatment instructions (numbered list)",
              "medicine": "Specific pesticides/fungicides available in Indian markets with dosage",
              "prevention": "Preventive measures to avoid recurrence",
              "explanation": "Brief explanation of the disease, its cause, and how it spreads"
            }
            
            Focus on treatments and medicines available in India. Be specific with product names, 
            dosages, and application methods. Respond ONLY with valid JSON.
            """, diseaseName, cropType != null ? cropType : "crop");
    }

    private DiseaseReport parseDiseaseReport(String json, User user, String imageUrl,
                                              String cropType, String diseaseName, Double confidence) {
        try {
            JsonNode node = objectMapper.readTree(json);
            return DiseaseReport.builder()
                    .user(user).imageUrl(imageUrl).cropType(cropType)
                    .diseaseName(node.path("diseaseName").asText(diseaseName))
                    .confidenceScore(confidence)
                    .treatment(node.path("treatment").asText())
                    .medicine(node.path("medicine").asText())
                    .prevention(node.path("prevention").asText())
                    .geminiExplanation(node.path("explanation").asText())
                    .isResolved(false).build();
        } catch (Exception e) {
            log.error("Failed to parse disease response: {}", e.getMessage());
            return DiseaseReport.builder()
                    .user(user).imageUrl(imageUrl).cropType(cropType)
                    .diseaseName(diseaseName).confidenceScore(confidence)
                    .geminiExplanation(json).isResolved(false).build();
        }
    }

    private DiseaseReportResponse toResponse(DiseaseReport report) {
        return DiseaseReportResponse.builder()
                .id(report.getId()).imageUrl(report.getImageUrl())
                .cropType(report.getCropType()).diseaseName(report.getDiseaseName())
                .confidenceScore(report.getConfidenceScore()).treatment(report.getTreatment())
                .medicine(report.getMedicine()).prevention(report.getPrevention())
                .geminiExplanation(report.getGeminiExplanation())
                .isResolved(report.isResolved()).createdAt(report.getCreatedAt()).build();
    }

    record HFPrediction(String label, Double score) {
        public String getLabel() { return label; }
        public Double getScore() { return score; }
    }
}
