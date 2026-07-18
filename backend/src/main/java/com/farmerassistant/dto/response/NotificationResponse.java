package com.farmerassistant.dto.response;

import com.farmerassistant.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private Notification.NotificationType type;
    private boolean isRead;
    private LocalDateTime createdAt;
}
