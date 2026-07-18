package com.farmerassistant.repository;

import com.farmerassistant.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByUserIdAndSessionIdOrderByCreatedAtAsc(Long userId, String sessionId);

    List<ChatMessage> findTop10ByUserIdAndSessionIdOrderByCreatedAtDesc(Long userId, String sessionId);

    @Query("SELECT DISTINCT c.sessionId FROM ChatMessage c WHERE c.user.id = :userId ORDER BY c.sessionId DESC")
    List<String> findDistinctSessionIdsByUserId(@Param("userId") Long userId);

    void deleteByUserIdAndSessionId(Long userId, String sessionId);

    @Query("SELECT c FROM ChatMessage c WHERE c.user.id = :userId AND c.sessionId = :sessionId ORDER BY c.createdAt DESC LIMIT 1")
    ChatMessage findLastMessageInSession(@Param("userId") Long userId, @Param("sessionId") String sessionId);
}
