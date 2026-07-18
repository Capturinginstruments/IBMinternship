package com.farmerassistant.service;

import com.farmerassistant.dto.request.CropRecommendationRequest;
import com.farmerassistant.dto.response.CropRecommendationResponse;
import com.farmerassistant.model.CropRecommendation;
import com.farmerassistant.model.User;
import com.farmerassistant.repository.CropRecommendationRepository;
import com.farmerassistant.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CropRecommendationServiceTest {

    @Mock
    private CropRecommendationRepository recommendationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GeminiService geminiService;

    @InjectMocks
    private CropRecommendationService cropRecommendationService;

    private User testUser;
    private CropRecommendationRequest testRequest;
    private CropRecommendation testRec;

    @BeforeEach
    public void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@farmer.com")
                .role(User.Role.FARMER)
                .build();

        testRequest = new CropRecommendationRequest();
        testRequest.setState("Maharashtra");
        testRequest.setDistrict("Pune");
        testRequest.setSeason(com.farmerassistant.model.CropRecommendation.Season.KHARIF);
        testRequest.setSoilType("Clay");
        testRequest.setNitrogen(40.0);
        testRequest.setPhosphorus(40.0);
        testRequest.setPotassium(40.0);
        testRequest.setTemperature(28.0);
        testRequest.setHumidity(70.0);
        testRequest.setRainfall(1000.0);
        testRequest.setPhLevel(6.5);

        testRec = CropRecommendation.builder()
                .id(1L)
                .user(testUser)
                .recommendedCrop("Rice")
                .confidenceScore(92.0)
                .expectedYield("30 q/ha")
                .profitEstimate("₹50k/ha")
                .waterRequirement("High")
                .fertilizerAdvice("N:P:K advice")
                .geminiExplanation("Soil suitable for Rice")
                .build();
    }

    @Test
    public void recommend_success() {
        String geminiJson = """
            {
              "recommendedCrop": "Rice",
              "confidenceScore": 92.0,
              "expectedYield": "30 q/ha",
              "profitEstimate": "₹50k/ha",
              "waterRequirement": "High",
              "fertilizerAdvice": "N:P:K advice",
              "explanation": "Soil suitable for Rice"
            }
            """;

        when(userRepository.findById(any())).thenReturn(Optional.of(testUser));
        when(geminiService.generateContent(anyString())).thenReturn(geminiJson);
        when(geminiService.extractJsonFromResponse(anyString())).thenReturn(geminiJson);
        when(recommendationRepository.save(any(CropRecommendation.class))).thenReturn(testRec);

        CropRecommendationResponse response = cropRecommendationService.recommend(testRequest, 1L);

        assertNotNull(response);
        assertEquals("Rice", response.getRecommendedCrop());
        assertEquals(92.0, response.getConfidenceScore());
        verify(recommendationRepository, times(1)).save(any());
    }

    @Test
    public void getById_success() {
        when(recommendationRepository.findById(1L)).thenReturn(Optional.of(testRec));

        CropRecommendationResponse response = cropRecommendationService.getById(1L, 1L);

        assertNotNull(response);
        assertEquals("Rice", response.getRecommendedCrop());
    }

    @Test
    public void getById_forbidden_throwsException() {
        when(recommendationRepository.findById(1L)).thenReturn(Optional.of(testRec));

        assertThrows(com.farmerassistant.exception.AccessForbiddenException.class, () ->
                cropRecommendationService.getById(1L, 2L)); // different userId
    }

    @Test
    public void getHistory_success() {
        Page<CropRecommendation> page = new PageImpl<>(List.of(testRec));
        when(recommendationRepository.findByUserIdOrderByCreatedAtDesc(anyLong(), any(PageRequest.class)))
                .thenReturn(page);

        Page<CropRecommendationResponse> responsePage = cropRecommendationService.getHistory(1L, PageRequest.of(0, 10));

        assertNotNull(responsePage);
        assertEquals(1, responsePage.getContent().size());
        assertEquals("Rice", responsePage.getContent().get(0).getRecommendedCrop());
    }
}
