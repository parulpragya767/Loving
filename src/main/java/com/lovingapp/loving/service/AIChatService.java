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
import com.lovingapp.loving.model.dto.RitualPackDTO;
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
        private final RitualRecommendationService ritualRecommendationService;

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
        public ChatDTOs.SendMessageResponse sendMessage(UUID sessionId,
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

        public ChatDTOs.SendMessageResponse recommendRitualPack(
                        UUID sessionId,
                        UUID userId,
                        ChatDTOs.SendMessageRequest request) {

                List<ChatMessage> messages = chatMessageRepository
                                .findBySessionIdOrderByCreatedAtAsc(sessionId);

                String extractionSystemPrompt = llmPromptHelper.generateUserContextExtractionPrompt(null);

                LLMRequest extractionRequest = LLMRequest.builder()
                                .messages(messages.stream()
                                                .map(m -> new LLMChatMessage(m.getRole(), m.getContent()))
                                                .collect(Collectors.toList()))
                                .systemPrompt(extractionSystemPrompt)
                                .responseFormat(LLMResponseFormat.TEXT)
                                .build();

                boolean validated = true;
                RitualPackDTO recommendedPack = null;

                // try {
                // // Extract user context
                // LLMResponse extractionResponse = llmClient.generate(extractionRequest);
                // JsonNode extracted = extractionResponse.getParsedJson();

                // // Parse and validate user context
                // UserContextDTO dto = userContextExtractor.parseAndValidate(extracted);

                // // Enrich with identifiers before persisting
                // dto.setUserId(userId);
                // dto.setConversationId(sessionId.toString());
                // userContextService.createUserContext(dto);
                // } catch (Exception ex) {
                // log.warn("UserContext extraction/validation/persistence failed: {}",
                // ex.getMessage());
                // validated = false;
                // }

                // Get ritual pack recommendation
                recommendedPack = ritualRecommendationService.recommendRitualPack(null)
                                .orElse(null);

                // Build a contextual wrap-up via LLM that ties the pack to the user's situation
                String responseMessage;
                try {
                        // Fetch love types for richer domain knowledge in the prompt
                        List<LoveTypeInfo> loveTypes = loveTypeRepository.findAll();

                        if (recommendedPack != null) {
                                String wrapUpSystemPrompt = llmPromptHelper.generateRitualWrapUpPrompt(loveTypes,
                                                recommendedPack);

                                LLMRequest wrapUpRequest = LLMRequest.builder()
                                                .messages(messages.stream()
                                                                .map(m -> new LLMChatMessage(m.getRole(),
                                                                                m.getContent()))
                                                                .collect(Collectors.toList()))
                                                .systemPrompt(wrapUpSystemPrompt)
                                                .responseFormat(LLMResponseFormat.TEXT)
                                                .build();

                                // LLMResponse wrapUpResponse = llmClient.generate(wrapUpRequest);
                                // JsonNode wrapUpNode = wrapUpResponse.getParsedJson();
                                // String llmWrap = wrapUpNode.path("response").asText(null);
                                String llmWrap = "";
                                responseMessage = (llmWrap != null && !llmWrap.isBlank())
                                                ? llmWrap
                                                : String.format("I recommend the '%s' ritual pack for you! %s",
                                                                recommendedPack.getTitle(),
                                                                recommendedPack.getShortDescription() != null
                                                                                ? recommendedPack.getShortDescription()
                                                                                : "");
                        } else {
                                // Fallback if no recommendation could be made
                                responseMessage = "I've taken in what you've shared. I don't have a specific ritual pack to suggest just yet, but we can keep exploring and I'll recommend something that fits as soon as I have enough context.";
                        }
                } catch (Exception ex) {
                        log.warn("Ritual wrap-up LLM generation failed: {}", ex.getMessage());
                        // Robust fallback
                        if (recommendedPack != null) {
                                responseMessage = String.format("I recommend the '%s' ritual pack for you! %s",
                                                recommendedPack.getTitle(),
                                                recommendedPack.getShortDescription() != null
                                                                ? recommendedPack.getShortDescription()
                                                                : "");
                        } else {
                                responseMessage = "I've analyzed your conversation. Here's a ritual pack that might interest you.";
                        }
                }

                // Create and save assistant message
                ChatMessage assistantMessage = ChatMessage.builder()
                                .sessionId(sessionId)
                                .role(ChatMessageRole.ASSISTANT)
                                .content(responseMessage)
                                .build();
                ChatMessage savedAssistantMessage = chatMessageRepository.save(assistantMessage);

                return ChatDTOs.SendMessageResponse.builder()
                                .assistantMessage(ChatMessageMapper.toDto(savedAssistantMessage))
                                .isReadyForRitualSuggestion(validated)
                                .recommendedRitualPack(recommendedPack)
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
