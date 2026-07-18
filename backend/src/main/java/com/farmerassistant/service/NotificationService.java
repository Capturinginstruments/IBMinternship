package com.farmerassistant.service;

import com.farmerassistant.dto.response.NotificationResponse;
import com.farmerassistant.model.Notification;
import com.farmerassistant.model.User;
import com.farmerassistant.repository.NotificationRepository;
import com.farmerassistant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final jakarta.persistence.EntityManager entityManager;

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public NotificationResponse markRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new com.farmerassistant.exception.ResourceNotFoundException("Notification not found with id: " + notificationId));
        if (!notification.getUser().getId().equals(userId))
            throw new com.farmerassistant.exception.AccessForbiddenException("Access denied: You do not own this notification");
        notification.setRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    public void markAllRead(Long userId) {
        notificationRepository.markAllReadForUser(userId);
    }

    public Notification createNotification(Long userId, String title, String message,
                                            Notification.NotificationType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.farmerassistant.exception.ResourceNotFoundException("User not found with id: " + userId));
        Notification n = Notification.builder()
                .user(user).title(title).message(message).type(type).isRead(false).build();
        return notificationRepository.save(n);
    }

    public void broadcastNotification(String title, String message, Notification.NotificationType type) {
        int pageSize = 500;
        int pageNumber = 0;
        Page<User> userPage;
        
        do {
            userPage = userRepository.findAll(org.springframework.data.domain.PageRequest.of(pageNumber, pageSize));
            for (User user : userPage.getContent()) {
                Notification n = Notification.builder()
                        .user(user).title(title).message(message).type(type).isRead(false).build();
                notificationRepository.save(n);
            }
            notificationRepository.flush();
            entityManager.clear();
            pageNumber++;
        } while (userPage.hasNext());
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId()).title(n.getTitle()).message(n.getMessage())
                .type(n.getType()).isRead(n.isRead()).createdAt(n.getCreatedAt()).build();
    }
}
