package com.farmerassistant.controller;

import com.farmerassistant.dto.request.ChatRequest;
import com.farmerassistant.dto.response.ApiResponse;
import com.farmerassistant.dto.response.ChatMessageResponse;
import com.farmerassistant.repository.UserRepository;
import com.farmerassistant.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "AI Chatbot", description = "KisanAI agricultural chatbot APIs")
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    @PostMapping("/message")
    @Operation(summary = "Send a message to KisanAI chatbot and get a response")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> chat(
            @RequestBody ChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        ChatMessageResponse response = chatService.chat(request, userId);
        return ResponseEntity.ok(ApiResponse.success("Message sent", response));
    }

    @GetMapping("/sessions")
    @Operation(summary = "Get all chat session IDs for current user")
    public ResponseEntity<ApiResponse<List<String>>> getSessions(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success(chatService.getUserSessions(userId)));
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Get full conversation history for a session")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getSessionHistory(
            @PathVariable String sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success(
                chatService.getSessionHistory(sessionId, userId)));
    }

    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "Delete a chat session")
    public ResponseEntity<ApiResponse<Void>> deleteSession(
            @PathVariable String sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        chatService.deleteSession(sessionId, userId);
        return ResponseEntity.ok(ApiResponse.success("Session deleted", null));
    }

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
    }
}
