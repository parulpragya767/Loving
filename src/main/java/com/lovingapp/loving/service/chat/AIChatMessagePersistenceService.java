package com.lovingapp.loving.service.chat;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.loving.model.domain.ChatMetadata;
import com.lovingapp.loving.model.entity.ChatMessage;
import com.lovingapp.loving.model.enums.ChatMessageRole;
import com.lovingapp.loving.repository.ChatMessageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for persisting chat messages (user, assistant, system) in the AI chat
 * system.
 * Handles all database operations related to chat message storage and
 * retrieval.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatMessagePersistenceService {

        private final ChatMessageRepository chatMessageRepository;

        @Transactional
        public ChatMessage saveUserMessage(UUID sessionId, String content) {
                ChatMessage userMessage = ChatMessage.builder()
                                .sessionId(sessionId)
                                .role(ChatMessageRole.USER)
                                .content(content)
                                .build();

                ChatMessage savedUserMessage = chatMessageRepository.saveAndFlush(userMessage);
                log.info("User chat message saved successfully sessionId={} chatMessageId={}", sessionId,
                                savedUserMessage.getId());

                return savedUserMessage;
        }

        @Transactional
        public ChatMessage saveAssistantMessage(UUID sessionId, String content) {
                ChatMessage assistantMessage = ChatMessage.builder()
                                .sessionId(sessionId)
                                .role(ChatMessageRole.ASSISTANT)
                                .content(content)
                                .build();

                ChatMessage savedAssistantMessage = chatMessageRepository.saveAndFlush(assistantMessage);
                log.info("Assistant message created sessionId={} chatMessageId={}", sessionId,
                                savedAssistantMessage.getId());

                return savedAssistantMessage;
        }

        /**
         * Saves a wrap-up message as an assistant message.
         */
        @Transactional
        public ChatMessage saveWrapUpMessage(UUID sessionId, String wrapUpMessage) {
                ChatMessage assistantMessage = ChatMessage.builder()
                                .sessionId(sessionId)
                                .role(ChatMessageRole.ASSISTANT)
                                .content(wrapUpMessage)
                                .build();

                ChatMessage savedAssistantMessage = chatMessageRepository.saveAndFlush(assistantMessage);

                log.info("Recommendation wrap-up message saved successfully sessionId={} chatMessageId={}", sessionId,
                                savedAssistantMessage.getId());

                return savedAssistantMessage;
        }

        /**
         * Saves a system message with recommendation metadata.
         */
        @Transactional
        public ChatMessage saveRecommendationMessage(UUID sessionId, UUID recommendationId) {
                ChatMessage recommendationMessage = ChatMessage.builder()
                                .sessionId(sessionId)
                                .role(ChatMessageRole.SYSTEM)
                                .content("Recommended ritual pack")
                                .metadata(ChatMetadata.builder()
                                                .recommendationId(recommendationId)
                                                .build())
                                .build();

                ChatMessage savedRecommendationMessage = chatMessageRepository.saveAndFlush(recommendationMessage);

                log.info(
                                "System chat message with recommendation metadata saved successfully sessionId={} chatMessageId={}",
                                sessionId,
                                savedRecommendationMessage.getId());

                return savedRecommendationMessage;
        }

        @Transactional(readOnly = true)
        public List<ChatMessage> findMessagesBySessionId(UUID sessionId) {
                return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        }

        @Transactional
        public void deleteMessagesBySessionId(UUID sessionId) {
                chatMessageRepository.deleteBySessionId(sessionId);
        }
}
