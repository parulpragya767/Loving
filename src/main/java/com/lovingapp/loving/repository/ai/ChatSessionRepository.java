package com.lovingapp.loving.repository.ai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lovingapp.loving.model.entity.ai.ChatSession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {
    List<ChatSession> findByUserIdOrderByUpdatedAtDesc(UUID userId);

    Optional<ChatSession> findByUserIdAndConversationId(UUID userId, String conversationId);
}
