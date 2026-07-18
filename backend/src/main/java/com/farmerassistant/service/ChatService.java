package com.farmerassistant.service;

import com.farmerassistant.dto.request.ChatRequest;
import com.farmerassistant.dto.response.ChatMessageResponse;
import com.farmerassistant.model.ChatMessage;
import com.farmerassistant.model.User;
import com.farmerassistant.repository.ChatMessageRepository;
import com.farmerassistant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are KisanAI, an expert agricultural advisor for Indian farmers created by the AI Farmer Assistant platform.
            You have deep knowledge of:
            - Crop cultivation for all Indian states and agro-climatic zones
            - Plant diseases and their organic and chemical treatments
            - Weather patterns and their impact on farming
            - Government agricultural schemes (PM-KISAN, PMFBY, KCC, etc.)
            - Market prices and selling strategies
            - Organic farming and sustainable agriculture
            - Water management and irrigation techniques
            - Fertilizer recommendations and soil health
            - Livestock management
            
            Always provide practical, actionable advice specific to Indian farming conditions.
            Use simple language that farmers can understand.
            When providing crop or fertilizer advice, mention quantities in Indian units (quintals, bigha, etc.) where appropriate.
            Respond in %s language.
            If asked in Hindi or Marathi, respond in that language.
            Be empathetic and encouraging to farmers.
            """;

    public ChatMessageResponse chat(ChatRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.farmerassistant.exception.ResourceNotFoundException("User not found with id: " + userId));

        String sessionId = StringUtils.hasText(request.getSessionId())
                ? request.getSessionId() : UUID.randomUUID().toString();

        String language = request.getLanguage() != null ? request.getLanguage() : "en";
        String languageName = switch (language) {
            case "hi" -> "Hindi";
            case "mr" -> "Marathi";
            default -> "English";
        };

        List<ChatMessage> history = chatMessageRepository
                .findTop10ByUserIdAndSessionIdOrderByCreatedAtDesc(userId, sessionId);

        String prompt = buildPrompt(request, history, languageName);

        String assistantReply;
        try {
            if (StringUtils.hasText(request.getImageBase64())) {
                assistantReply = geminiService.generateContentWithImage(
                        prompt, request.getImageBase64(), request.getImageMimeType());
            } else {
                assistantReply = geminiService.generateContent(prompt);
            }
        } catch (Exception e) {
            log.error("AI response generation failed: {}", e.getMessage());
            assistantReply = "I'm KisanAI, your agricultural assistant! 🌱 I'm having trouble connecting to my AI brain right now, but I can still help you with farming advice. Try asking me about specific crops, diseases, fertilizers, or government schemes!";
        }

        ChatMessage userMessage = ChatMessage.builder()
                .user(user).sessionId(sessionId).role(ChatMessage.ChatRole.USER)
                .message(request.getMessage()).language(language).build();
        chatMessageRepository.save(userMessage);

        ChatMessage assistantMessage = ChatMessage.builder()
                .user(user).sessionId(sessionId).role(ChatMessage.ChatRole.ASSISTANT)
                .message(assistantReply).language(language).build();
        assistantMessage = chatMessageRepository.save(assistantMessage);

        return toResponse(assistantMessage);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getSessionHistory(String sessionId, Long userId) {
        return chatMessageRepository.findByUserIdAndSessionIdOrderByCreatedAtAsc(userId, sessionId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getUserSessions(Long userId) {
        return chatMessageRepository.findDistinctSessionIdsByUserId(userId);
    }

    public void deleteSession(String sessionId, Long userId) {
        chatMessageRepository.deleteByUserIdAndSessionId(userId, sessionId);
    }

    private String buildPrompt(ChatRequest request, List<ChatMessage> history, String languageName) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(String.format(SYSTEM_PROMPT_TEMPLATE, languageName)).append("\n\n");

        if (!history.isEmpty()) {
            prompt.append("Previous conversation context:\n");
            List<ChatMessage> orderedHistory = history.stream()
                    .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                    .collect(Collectors.toList());
            for (ChatMessage msg : orderedHistory) {
                prompt.append(msg.getRole().name()).append(": ").append(msg.getMessage()).append("\n");
            }
            prompt.append("\n");
        }

        prompt.append("FARMER: ").append(request.getMessage()).append("\n\nKISANAI:");
        return prompt.toString();
    }

    private ChatMessageResponse toResponse(ChatMessage msg) {
        return ChatMessageResponse.builder()
                .id(msg.getId()).sessionId(msg.getSessionId()).role(msg.getRole())
                .message(msg.getMessage()).imageUrl(msg.getImageUrl())
                .language(msg.getLanguage()).createdAt(msg.getCreatedAt()).build();
    }
}
