package com.lovingapp.loving.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.loving.client.LlmClient;
import com.lovingapp.loving.exception.ResourceNotFoundException;
import com.lovingapp.loving.helpers.ai.LLMPromptHelper;
import com.lovingapp.loving.mapper.ChatMessageMapper;
import com.lovingapp.loving.mapper.ChatSessionMapper;
import com.lovingapp.loving.model.domain.ai.LLMChatMessage;
import com.lovingapp.loving.model.domain.ai.LLMEmpatheticResponse;
import com.lovingapp.loving.model.domain.ai.LLMRequest;
import com.lovingapp.loving.model.domain.ai.LLMResponse;
import com.lovingapp.loving.model.domain.ai.LLMResponseFormat;
import com.lovingapp.loving.model.domain.ai.LLMUserContextExtraction;
import com.lovingapp.loving.model.dto.ChatDTOs.ChatSessionDTO;
import com.lovingapp.loving.model.dto.ChatDTOs.SendMessageRequest;
import com.lovingapp.loving.model.dto.ChatDTOs.SendMessageResponse;
import com.lovingapp.loving.model.dto.ChatDTOs.RecommendRitualPackResponse;
import com.lovingapp.loving.model.dto.RitualPackDTO;
import com.lovingapp.loving.model.dto.UserContextDTO;
import com.lovingapp.loving.model.entity.ChatMessage;
import com.lovingapp.loving.model.entity.ChatSession;
import com.lovingapp.loving.model.enums.ChatMessageRole;
import com.lovingapp.loving.repository.ChatMessageRepository;
import com.lovingapp.loving.repository.ChatSessionRepository;

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
    private final UserContextService userContextService;
    private final RitualRecommendationService ritualRecommendationService;

    @Transactional
    public ChatSessionDTO startSession(UUID userId, ChatSessionDTO request) {
        ChatSession session = ChatSession.builder()
                .userId(userId)
                .build();
        return ChatSessionMapper.toDto(chatSessionRepository.save(session));
    }

    @Transactional
    public SendMessageResponse sendMessage(UUID sessionId,
            SendMessageRequest request) {
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

        // Build empathetic chat system prompt and request from conversation history
        // (structured JSON)
        LLMRequest llmRequest = LLMRequest.builder()
                .messages(messages.stream()
                        .map(m -> new LLMChatMessage(m.getRole(), m.getContent()))
                        .collect(Collectors.toList()))
                .systemPrompt(LLMPromptHelper.generateEmpatheticChatResponsePrompt())
                .responseFormat(LLMResponseFormat.JSON)
                .build();

        LLMResponse<LLMEmpatheticResponse> aiReply = llmClient.generate(llmRequest,
                LLMEmpatheticResponse.class);
        LLMEmpatheticResponse empatheticResponse = aiReply.getParsed();
        String response = empatheticResponse.getResponse();
        boolean ready = empatheticResponse.isReadyForRitualSuggestion();

        ChatMessage assistantMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .role(ChatMessageRole.ASSISTANT)
                .content(response)
                .build();
        ChatMessage savedAssistantMessage = chatMessageRepository.save(assistantMessage);

        return SendMessageResponse.builder()
                .assistantResponse(ChatMessageMapper.toDto(savedAssistantMessage))
                .isReadyForRitualPackRecommendation(ready)
                .build();
    }

    public RecommendRitualPackResponse recommendRitualPack(
            UUID userId,
            UUID sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        List<ChatMessage> messages = chatMessageRepository
                .findBySessionIdOrderByCreatedAtAsc(sessionId);

        LLMRequest extractionRequest = LLMRequest.builder()
                .messages(messages.stream()
                        .map(m -> new LLMChatMessage(m.getRole(), m.getContent()))
                        .collect(Collectors.toList()))
                .systemPrompt(LLMPromptHelper.generateUserContextExtractionPrompt())
                .responseFormat(LLMResponseFormat.JSON)
                .build();

        LLMResponse<LLMUserContextExtraction> llmUserContextResponse = llmClient.generate(extractionRequest,
                LLMUserContextExtraction.class);
        LLMUserContextExtraction llmUserContext = llmUserContextResponse.getParsed();

        UserContextDTO userContext = UserContextDTO.builder()
                .userId(userId)
                .conversationId(sessionId)
                .journey(llmUserContext.getJourney())
                .loveTypes(llmUserContext.getLoveTypes())
                .relationalNeeds(llmUserContext.getRelationalNeeds())
                .relationshipStatus(llmUserContext.getRelationshipStatus())
                .semanticSummary(llmUserContext.getSemanticSummary())
                .build();

        UserContextDTO savedUserContext = userContextService.createUserContext(userContext);
        log.info("User context saved successfully : {}", savedUserContext.getId());

        // Update session title if not already set
        if (llmUserContext.getConversationTitle() != null && !llmUserContext.getConversationTitle().isBlank()
                && (session.getTitle() == null || session.getTitle().isBlank())) {
            String suggestedTitle = llmUserContext.getConversationTitle().trim();
            session.setTitle(suggestedTitle);
            log.info("Session title updated via JPA dirty checking.");
        }

        RitualPackDTO recommendedPack = null;

        // Get ritual pack recommendation
        recommendedPack = ritualRecommendationService.recommendRitualPack(savedUserContext)
                .orElse(null);

        // Build a contextual wrap-up via LLM that ties the pack to the user's situation
        String wrapUpMessage = "";

        try {
            if (recommendedPack != null) {
                LLMRequest wrapUpRequest = LLMRequest.builder()
                        .messages(messages.stream()
                                .map(m -> new LLMChatMessage(m.getRole(),
                                        m.getContent()))
                                .collect(Collectors.toList()))
                        .systemPrompt(LLMPromptHelper
                                .generateWrapUpChatResponsePrompt(recommendedPack))
                        .responseFormat(LLMResponseFormat.TEXT)
                        .build();

                LLMResponse<String> wrapUpResponse = llmClient.generate(wrapUpRequest);
                wrapUpMessage = wrapUpResponse.getRawText();
            }

            if (wrapUpMessage == null || wrapUpMessage.trim().isEmpty()) {
                wrapUpMessage = getFallbackWrapUpMessage(recommendedPack);
            }
        } catch (Exception ex) {
            log.warn("Ritual wrap-up LLM generation failed: {}", ex.getMessage());
            wrapUpMessage = getFallbackWrapUpMessage(recommendedPack);
        }

        // Create and save assistant message
        ChatMessage assistantMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .role(ChatMessageRole.ASSISTANT)
                .content(wrapUpMessage)
                .build();
        ChatMessage savedAssistantMessage = chatMessageRepository.save(assistantMessage);

        return RecommendRitualPackResponse.builder()
                .ritualPack(recommendedPack)
                .wrapUpResponse(ChatMessageMapper.toDto(savedAssistantMessage))
                .build();
    }

    @Transactional(readOnly = true)
    public ChatSessionDTO getChatSessionWithMessages(UUID sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        ChatSessionDTO chatSessionDto = ChatSessionMapper.toDto(session);
        chatSessionDto.setMessages(messages.stream().map(ChatMessageMapper::toDto).collect(Collectors.toList()));
        return chatSessionDto;
    }

    @Transactional(readOnly = true)
    public List<String> getSamplePrompts(UUID userId) {
        return getFallbackPrompts();
    }

    /**
     * Get fallback prompts in case the LLM call fails.
     */
    private List<String> getFallbackPrompts() {
        return Arrays.asList(
                "What's one small thing I can do today to make my partner feel appreciated?",
                "How can we improve our communication when we disagree?",
                "What's a fun activity we could try together this weekend?");
    }

    /**
     * Get fallback wrap-up messages in case the LLM call fails.
     */
    private String getFallbackWrapUpMessage(RitualPackDTO recommendedPack) {
        String wrapUpMessage;

        if (recommendedPack != null) {
            wrapUpMessage = String.format("I recommend the '%s' ritual pack for you! %s",
                    recommendedPack.getTitle(),
                    recommendedPack.getTagLine() != null ? recommendedPack.getTagLine() : "");
        } else {
            wrapUpMessage = "I've analyzed your conversation. Here's a ritual pack that might interest you.";
        }

        // Fallback if no recommendation could be made
        // wrapUpMessage = "I've taken in what you've shared. I don't have a specific
        // ritual pack to suggest just yet, but we can keep exploring and I'll recommend
        // something that fits as soon as I have enough context.";

        return wrapUpMessage;
    }

    @Transactional(readOnly = true)
    public List<ChatSessionDTO> listSessions(UUID userId) {
        return chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(ChatSessionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSession(UUID userId, UUID sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!session.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Session not found");
        }

        // Delete messages first to avoid FK constraints if any
        chatMessageRepository.deleteBySessionId(sessionId);
        // Then delete the session
        chatSessionRepository.deleteById(sessionId);
    }
}
