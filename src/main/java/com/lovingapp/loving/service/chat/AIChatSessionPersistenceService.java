package com.lovingapp.loving.service.chat;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.loving.exception.ResourceNotFoundException;
import com.lovingapp.loving.mapper.ChatSessionMapper;
import com.lovingapp.loving.model.dto.ChatDTOs.ChatSessionDTO;
import com.lovingapp.loving.model.entity.ChatSession;
import com.lovingapp.loving.repository.ChatSessionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing chat session persistence operations.
 * Handles CRUD operations for chat sessions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatSessionPersistenceService {

    private final ChatSessionRepository chatSessionRepository;
    private final AIChatMessagePersistenceService chatMessagePersistenceService;

    @Transactional
    public ChatSessionDTO createSession(UUID userId) {
        ChatSession session = ChatSession.builder()
                .userId(userId)
                .build();
        ChatSession saved = chatSessionRepository.saveAndFlush(session);
        return ChatSessionMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public ChatSession findSessionByIdAndUserId(UUID sessionId, UUID userId) {
        return chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatSession", "id", sessionId));
    }

    @Transactional(readOnly = true)
    public List<ChatSessionDTO> listSessions(UUID userId) {
        return chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(ChatSessionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSession(UUID userId, UUID sessionId) {
        chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatSession", "id", sessionId));

        // Delete messages first to avoid FK constraints if any
        chatMessagePersistenceService.deleteMessagesBySessionId(sessionId);
        // Then delete the session
        chatSessionRepository.deleteById(sessionId);
    }

    /**
     * Update session title if not already set and a title is available from user
     * context.
     */
    public void updateSessionTitle(ChatSession session, String conversationTitle) {
        if (conversationTitle != null && !conversationTitle.isBlank()
                && (session.getTitle() == null || session.getTitle().isBlank())) {
            String suggestedTitle = conversationTitle.trim();
            session.setTitle(suggestedTitle);
            log.info("Session title updated via JPA dirty checking sessionId={}", session.getId());
        }
    }
}
