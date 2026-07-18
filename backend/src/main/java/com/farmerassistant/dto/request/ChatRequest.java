package com.farmerassistant.dto.request;

import lombok.Data;

@Data
public class ChatRequest {
    private String sessionId;
    private String message;
    private String imageBase64;
    private String imageMimeType;
    private String language = "en";
}
