package com.farmerassistant.dto.response;

import com.farmerassistant.model.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private String sessionId;
    private ChatMessage.ChatRole role;
    private String message;
    private String imageUrl;
    private String language;
    private LocalDateTime createdAt;
}
