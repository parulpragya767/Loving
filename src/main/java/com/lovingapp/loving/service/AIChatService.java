package com.lovingapp.loving.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.loving.model.domain.LlmResponse;
import com.lovingapp.loving.model.dto.ChatDTOs;
import com.lovingapp.loving.model.dto.UserContextDTO;
import com.lovingapp.loving.model.entity.ChatMessage;
import com.lovingapp.loving.model.entity.ChatSession;
import com.lovingapp.loving.model.enums.ChatMessageRole;
import com.lovingapp.loving.model.enums.ChatSessionStatus;
import com.lovingapp.loving.repository.ChatMessageRepository;
import com.lovingapp.loving.repository.ChatSessionRepository;
import com.lovingapp.loving.service.ai.AIChatPrompts;
import com.lovingapp.loving.service.ai.LlmClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Service implementation for managing AI chat sessions and messages.
 * Handles the conversation flow with the LLM and context extraction.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatService {

        private final ChatSessionRepository sessionRepository;
        private final ChatMessageRepository messageRepository;
        private final LlmClient llmClient;
        private final AIChatPrompts aiChatPrompts;

        @Transactional
        public Mono<ChatDTOs.StartSessionResponse> startSession(ChatDTOs.StartSessionRequest request) {
                UUID userId = request.getUserId();
                UUID sessionId = request.getSessionId();
                String conversationTitle = request.getConversationTitle();

                // Try to find existing session with this conversation ID
                Mono<ChatSession> sessionMono = conversationTitle != null
                                ? Mono.fromCallable(() -> sessionRepository
                                                .findById(sessionId)
                                                .orElseGet(() -> createNewSession(userId, conversationTitle)))
                                : Mono.fromCallable(() -> createNewSession(userId, null));

                return sessionMono.flatMap(session -> {
                        // Get existing messages for this session
                        List<ChatMessage> existingMessages = messageRepository
                                        .findBySessionIdOrderByCreatedAtAsc(session.getId());

                        // If this is a new session or has no messages, add the system prompt
                        if (existingMessages.isEmpty()) {
                                String systemPrompt = request.getSystemPrompt() != null
                                                ? request.getSystemPrompt()
                                                : aiChatPrompts.generateSystemPrompt();

                                ChatMessage systemMessage = ChatMessage.builder()
                                                .sessionId(session.getId())
                                                .role(ChatMessageRole.SYSTEM)
                                                .content(systemPrompt)
                                                .build();

                                messageRepository.save(systemMessage);
                                existingMessages.add(systemMessage);
                        }

                        return Mono.just(ChatDTOs.StartSessionResponse.builder()
                                        .sessionId(session.getId())
                                        .conversationTitle(session.getConversationTitle())
                                        .messages(existingMessages.stream()
                                                        .map(this::toDto)
                                                        .collect(Collectors.toList()))
                                        .build());
                });
        }

        @Transactional
        public Mono<ChatDTOs.SendMessageResponse> sendMessage(UUID sessionId, ChatDTOs.SendMessageRequest request) {
                // 1. Save user message
                ChatMessage userMessage = ChatMessage.builder()
                                .sessionId(sessionId)
                                .role(ChatMessageRole.USER)
                                .content(request.getContent())
                                .build();

                return Mono.fromCallable(() -> messageRepository.save(userMessage))
                                .flatMap(savedUserMessage -> {
                                        // 2. Get conversation history
                                        List<ChatMessage> messages = messageRepository
                                                        .findBySessionIdOrderByCreatedAtAsc(sessionId);

                                        // 3. Convert to LLM message format
                                        List<LlmClient.Message> llmMessages = messages.stream()
                                                        .map(m -> new LlmClient.Message(
                                                                        m.getRole().name().toLowerCase(),
                                                                        m.getContent()))
                                                        .collect(Collectors.toList());

                                        // 4. Get AI response and process it
                                        return llmClient.chat(llmMessages)
                                                        .flatMap(llmResponse -> {
                                                                // 5. Save assistant's response
                                                                ChatMessage assistantMessage = ChatMessage.builder()
                                                                                .sessionId(sessionId)
                                                                                .role(ChatMessageRole.ASSISTANT)
                                                                                .content(llmResponse.getResponse())
                                                                                .build();

                                                                ChatMessage savedAssistantMessage = messageRepository
                                                                                .save(assistantMessage);

                                                                // 6. Process the context from LLM response
                                                                LlmResponse.Context context = llmResponse.getContext();
                                                                UserContextDTO userContext = UserContextDTO.builder()
                                                                                .emotionalStates(context
                                                                                                .getEmotionalStates())
                                                                                .preferredLoveLanguages(
                                                                                                context.getLoveTypes())
                                                                                .build();

                                                                // 7. If we have enough context, trigger recommendations
                                                                boolean recommendationTriggered = false;
                                                                if (context.isReadyForRecommendation()) {
                                                                        recommendationTriggered = recommendationServiceTrigger(
                                                                                        userContext);

                                                                        // Update session status if we're done
                                                                        sessionRepository.findById(sessionId)
                                                                                        .ifPresent(session -> {
                                                                                                session.setStatus(
                                                                                                                ChatSessionStatus.COMPLETED);
                                                                                                sessionRepository.save(
                                                                                                                session);
                                                                                        });
                                                                }

                                                                // 8. Return the response
                                                                return Mono.just(ChatDTOs.SendMessageResponse.builder()
                                                                                .assistantMessage(toDto(
                                                                                                savedAssistantMessage))
                                                                                .askedFollowUp(context
                                                                                                .isNeedsFollowUp())
                                                                                .recommendationTriggered(
                                                                                                recommendationTriggered)
                                                                                .build());
                                                        });
                                });
        }

        @Transactional(readOnly = true)
        public Mono<ChatDTOs.GetHistoryResponse> getChatHistory(UUID sessionId) {
                return Mono.fromCallable(() -> {
                        List<ChatMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
                        return ChatDTOs.GetHistoryResponse.builder()
                                        .messages(messages.stream()
                                                        .map(this::toDto)
                                                        .collect(Collectors.toList()))
                                        .build();
                });
        }

        // === Private helper methods ===

        private boolean recommendationServiceTrigger(UserContextDTO context) {
                // Implement recommendation service trigger logic here
                // For now, just log and return false
                log.info("Recommendation service triggered with context: {}", context);
                return false;
        }

        private ChatSession createNewSession(UUID userId, String conversationId) {
                ChatSession session = ChatSession.builder()
                                .userId(userId)
                                .conversationTitle(
                                                conversationId != null ? conversationId : UUID.randomUUID().toString())
                                .status(ChatSessionStatus.ACTIVE)
                                .build();
                return sessionRepository.save(session);
        }

        private ChatDTOs.ChatMessageDTO toDto(ChatMessage message) {
                return ChatDTOs.ChatMessageDTO.builder()
                                .id(message.getId())
                                .sessionId(message.getSessionId())
                                .role(message.getRole())
                                .content(message.getContent())
                                .createdAt(message.getCreatedAt())
                                .build();
        }
}
