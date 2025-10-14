package com.lovingapp.loving.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.lovingapp.loving.client.LlmClient;
import com.lovingapp.loving.helpers.ai.LLMPromptHelper;
import com.lovingapp.loving.helpers.ai.UserContextExtractor;
import com.lovingapp.loving.mapper.ChatMessageMapper;
import com.lovingapp.loving.model.domain.ai.LLMChatMessage;
import com.lovingapp.loving.model.domain.ai.LLMRequest;
import com.lovingapp.loving.model.domain.ai.LLMResponse;
import com.lovingapp.loving.model.domain.ai.LLMResponseFormat;
import com.lovingapp.loving.model.dto.ChatDTOs;
import com.lovingapp.loving.model.dto.UserContextDTO;
import com.lovingapp.loving.model.entity.ChatMessage;
import com.lovingapp.loving.model.entity.ChatSession;
import com.lovingapp.loving.model.entity.LoveTypeInfo;
import com.lovingapp.loving.model.enums.ChatMessageRole;
import com.lovingapp.loving.model.enums.ChatSessionStatus;
import com.lovingapp.loving.repository.ChatMessageRepository;
import com.lovingapp.loving.repository.ChatSessionRepository;
import com.lovingapp.loving.repository.LoveTypeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation for managing AI chat sessions and messages.
 * Handles the conversation flow with the LLM and context extraction.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatService {

        private final ChatSessionRepository chatSessionRepository;
        private final ChatMessageRepository chatMessageRepository;
        private final LlmClient llmClient;
        private final LLMPromptHelper llmPromptHelper;
        private final UserContextExtractor userContextExtractor;
        private final LoveTypeRepository loveTypeRepository;
        private final UserContextService userContextService;

        @Transactional
        public ChatDTOs.StartSessionResponse startSession(ChatDTOs.StartSessionRequest request) {
                UUID userId = request.getUserId();
                UUID sessionId = request.getSessionId();
                String conversationTitle = request.getConversationTitle();

                // Get or create session
                ChatSession session = (sessionId != null)
                                ? chatSessionRepository.findById(sessionId)
                                                .orElseGet(() -> createNewSession(userId, conversationTitle))
                                : createNewSession(userId, conversationTitle);

                // Get existing messages for this session
                List<ChatMessage> existingMessages = chatMessageRepository
                                .findBySessionIdOrderByCreatedAtAsc(session.getId());

                return ChatDTOs.StartSessionResponse.builder()
                                .sessionId(session.getId())
                                .conversationTitle(session.getConversationTitle())
                                .messages(existingMessages.stream()
                                                .map(ChatMessageMapper::toDto)
                                                .collect(Collectors.toList()))
                                .build();
        }

        @Transactional
        public ChatDTOs.SendMessageResponse sendMessage(UUID sessionId, UUID userId,
                        ChatDTOs.SendMessageRequest request) {
                // 1. Save user message
                ChatMessage userMessage = ChatMessage.builder()
                                .sessionId(sessionId)
                                .role(ChatMessageRole.USER)
                                .content(request.getContent())
                                .build();

                chatMessageRepository.save(userMessage);

                // 2. Get conversation history
                List<ChatMessage> messages = chatMessageRepository
                                .findBySessionIdOrderByCreatedAtAsc(sessionId);

                if (!request.isReadyForRitualSuggestion()) {
                        return handleEmpatheticFlow(sessionId, messages);
                } else {
                        return handleContextExtractionFlow(sessionId, userId, request, messages);
                }
        }

        private ChatDTOs.SendMessageResponse handleEmpatheticFlow(UUID sessionId,
                        List<ChatMessage> messages) {

                // 2.1 Get all love types for the prompt
                List<LoveTypeInfo> loveTypes = loveTypeRepository.findAll();

                // Build empathetic chat system prompt and request from conversation history
                LLMRequest llmRequest = LLMRequest.builder()
                                .messages(messages.stream()
                                                .map(m -> new LLMChatMessage(m.getRole(), m.getContent()))
                                                .collect(Collectors.toList()))
                                .systemPrompt(llmPromptHelper.generateEmpatheticChatResponsePrompt(loveTypes))
                                .responseFormat(LLMResponseFormat.TEXT)
                                .build();

                LLMResponse aiReply = llmClient.generate(llmRequest);
                JsonNode node = aiReply.getParsedJson();
                String response = node.path("response").asText();

                ChatMessage assistantMessage = ChatMessage.builder()
                                .sessionId(sessionId)
                                .role(ChatMessageRole.ASSISTANT)
                                .content(response)
                                .build();
                ChatMessage savedAssistantMessage = chatMessageRepository.save(assistantMessage);

                return ChatDTOs.SendMessageResponse.builder()
                                .assistantMessage(ChatMessageMapper.toDto(savedAssistantMessage))
                                .isReadyForRitualSuggestion(false)
                                .build();
        }

        private ChatDTOs.SendMessageResponse handleContextExtractionFlow(
                        UUID sessionId,
                        UUID userId,
                        ChatDTOs.SendMessageRequest request,
                        List<ChatMessage> messages) {
                // Build a structured conversation summary
                String conversationSummary = messages.stream()
                                .map(msg -> String.format("%s: %s",
                                                msg.getRole().name().toLowerCase(),
                                                msg.getContent()))
                                .collect(Collectors.joining("\n"));

                String extractionSystemPrompt = llmPromptHelper.generateUserContextExtractionPrompt(
                                conversationSummary,
                                null);

                LLMRequest extractionRequest = LLMRequest.builder()
                                .systemPrompt(extractionSystemPrompt)
                                .responseFormat(LLMResponseFormat.JSON)
                                .build();

                boolean validated = true;
                try {
                        LLMResponse extractionResponse = llmClient.generate(extractionRequest);
                        JsonNode extracted = extractionResponse.getParsedJson();
                        // Parse and validate; will throw on invalid values
                        UserContextDTO dto = userContextExtractor.parseAndValidate(extracted);
                        // Enrich with identifiers before persisting
                        dto.setUserId(userId);
                        dto.setConversationId(sessionId.toString());
                        userContextService.createUserContext(dto);
                } catch (Exception ex) {
                        log.warn("UserContext extraction/validation/persistence failed: {}", ex.getMessage());
                        validated = false;
                }

                ChatMessage assistantMessage = ChatMessage.builder()
                                .sessionId(sessionId)
                                .role(ChatMessageRole.ASSISTANT)
                                .content("Ritual Pack recommended")
                                .build();
                // ChatMessage savedAssistantMessage =
                // chatMessageRepository.save(assistantMessage);

                return ChatDTOs.SendMessageResponse.builder()
                                .assistantMessage(ChatMessageMapper.toDto(assistantMessage))
                                .isReadyForRitualSuggestion(validated)
                                .build();
        }

        @Transactional(readOnly = true)
        public ChatDTOs.GetHistoryResponse getChatHistory(UUID sessionId) {
                List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
                return ChatDTOs.GetHistoryResponse.builder()
                                .sessionId(sessionId)
                                .messages(messages.stream()
                                                .map(ChatMessageMapper::toDto)
                                                .collect(Collectors.toList()))
                                .build();
        }

        private ChatSession createNewSession(UUID userId, String conversationTitle) {
                ChatSession session = ChatSession.builder()
                                .userId(userId)
                                .conversationTitle(conversationTitle)
                                .status(ChatSessionStatus.ACTIVE)
                                .build();
                return chatSessionRepository.save(session);
        }

}
