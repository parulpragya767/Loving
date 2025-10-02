package com.lovingapp.loving.repository.ai;

import com.lovingapp.loving.model.ai.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {
    List<ChatSession> findByUserIdOrderByUpdatedAtDesc(UUID userId);
    Optional<ChatSession> findByUserIdAndConversationId(UUID userId, String conversationId);
}
