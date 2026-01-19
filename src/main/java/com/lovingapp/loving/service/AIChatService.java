package com.lovingapp.loving.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.loving.mapper.ChatMessageMapper;
import com.lovingapp.loving.mapper.ChatSessionMapper;
import com.lovingapp.loving.model.domain.ai.LLMEmpatheticResponse;
import com.lovingapp.loving.model.domain.ai.LLMUserContextExtraction;
import com.lovingapp.loving.model.dto.ChatDTOs.ChatSessionDTO;
import com.lovingapp.loving.model.dto.ChatDTOs.RecommendRitualPackResponse;
import com.lovingapp.loving.model.dto.ChatDTOs.SendMessageRequest;
import com.lovingapp.loving.model.dto.ChatDTOs.SendMessageResponse;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.RitualHistoryDTO;
import com.lovingapp.loving.model.dto.RitualPackDTO;
import com.lovingapp.loving.model.dto.UserContextDTOs.UserContextCreateRequest;
import com.lovingapp.loving.model.dto.UserContextDTOs.UserContextDTO;
import com.lovingapp.loving.model.entity.ChatMessage;
import com.lovingapp.loving.model.entity.ChatSession;
import com.lovingapp.loving.service.chat.AIChatLLMHelper;
import com.lovingapp.loving.service.chat.AIChatMessagePersistenceService;
import com.lovingapp.loving.service.chat.AIChatRitualRecommendationAndHistoryHelper;
import com.lovingapp.loving.service.chat.AIChatSessionPersistenceService;

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

	private final AIChatSessionPersistenceService chatSessionPersistenceService;
	private final AIChatMessagePersistenceService chatMessagePersistenceService;
	private final UserContextService userContextService;
	private final RecommendationEngine recommendationEngine;
	private final AIChatLLMHelper aiChatLLMHelper;
	private final AIChatRitualRecommendationAndHistoryHelper ritualRecommendationAndHistoryHelper;

	@Transactional
	public ChatSessionDTO createSession(UUID userId) {
		return chatSessionPersistenceService.createSession(userId);
	}

	@Transactional
	public SendMessageResponse sendMessage(UUID userId, UUID sessionId, SendMessageRequest request) {
		// 0. Validate session exists and belongs to the user
		chatSessionPersistenceService.findSessionByIdAndUserId(sessionId, userId);

		// 1. Save user message
		chatMessagePersistenceService.saveUserMessage(sessionId, request.getContent());

		// 2. Get conversation history
		List<ChatMessage> messages = chatMessagePersistenceService.findMessagesBySessionId(sessionId);

		// 3. Generate empathetic response from the conversation history using LLM
		LLMEmpatheticResponse empatheticResponse = aiChatLLMHelper.generateEmpatheticResponse(sessionId, messages);
		String response = empatheticResponse.getResponse();
		boolean ready = empatheticResponse.isReadyForRitualSuggestion();

		// 4. Create and save assistant message
		ChatMessage savedAssistantMessage = chatMessagePersistenceService.saveAssistantMessage(sessionId, response);
		log.info("Assistant message created sessionId={} chatMessageId={} readyForRecommendation={}", sessionId,
				savedAssistantMessage.getId(), ready);

		return SendMessageResponse.builder()
				.assistantResponse(ChatMessageMapper.toDto(savedAssistantMessage))
				.isReadyForRitualPackRecommendation(ready)
				.build();
	}

	@Transactional
	public RecommendRitualPackResponse recommendRitualPack(UUID userId, UUID sessionId) {
		// Validate session exists and belongs to user and fetch chat messages
		ChatSession session = chatSessionPersistenceService.findSessionByIdAndUserId(sessionId, userId);

		List<ChatMessage> messages = chatMessagePersistenceService.findMessagesBySessionId(sessionId);

		// Extract user context from conversation using LLM
		LLMUserContextExtraction extractedUserContext = aiChatLLMHelper.extractUserContext(userId, sessionId, messages);

		// Save user context
		UserContextDTO savedUserContext = saveUserContext(userId, sessionId, extractedUserContext);

		// Update session title if needed
		chatSessionPersistenceService.updateSessionTitle(session, extractedUserContext.getConversationTitle());

		// Get ritual pack recommendation
		RitualPackDTO recommendedPack = getRitualPackRecommendation(savedUserContext);

		// Generate wrap-up message using LLM
		String wrapUpMessage = aiChatLLMHelper.generateWrapUpMessage(messages, recommendedPack, sessionId);

		// Save wrap-up message
		ChatMessage savedAssistantMessage = chatMessagePersistenceService.saveWrapUpMessage(sessionId, wrapUpMessage);

		// Create recommendation and history records
		List<RitualHistoryDTO> createdHistories = ritualRecommendationAndHistoryHelper.createRecommendationAndHistory(
				userId, sessionId, recommendedPack);

		return RecommendRitualPackResponse.builder()
				.ritualPack(recommendedPack)
				.wrapUpResponse(ChatMessageMapper.toDto(savedAssistantMessage))
				.ritualHistoryMap(createdHistories.stream()
						.collect(Collectors.toMap(RitualHistoryDTO::getRitualId, Function.identity())))
				.build();
	}

	@Transactional(readOnly = true)
	public ChatSessionDTO getChatSessionWithMessages(UUID userId, UUID sessionId) {
		ChatSession session = chatSessionPersistenceService.findSessionByIdAndUserId(sessionId, userId);

		List<ChatMessage> messages = chatMessagePersistenceService.findMessagesBySessionId(sessionId);
		log.info("Chat messages fetched successfully sessionId={}, messagesCount={}", sessionId,
				messages.size());

		ChatSessionDTO chatSessionDto = ChatSessionMapper.toDto(session);
		chatSessionDto.setMessages(messages.stream()
				.map(ChatMessageMapper::toDto)
				.collect(Collectors.toList()));
		return chatSessionDto;
	}

	public List<String> getSamplePrompts() {
		return Arrays.asList(
				"What's one small thing I can do today to make my partner feel appreciated?",
				"How can we improve our communication when we disagree?",
				"What's a fun activity we could try together this weekend?");
	}

	@Transactional(readOnly = true)
	public List<ChatSessionDTO> listSessions(UUID userId) {
		return chatSessionPersistenceService.listSessions(userId);
	}

	@Transactional
	public void deleteSession(UUID userId, UUID sessionId) {
		chatSessionPersistenceService.deleteSession(userId, sessionId);
		userContextService.deleteByUserIdAndConversationId(userId, sessionId);
	}

	/**
	 * Create and save user context.
	 */
	private UserContextDTO saveUserContext(UUID userId, UUID sessionId, LLMUserContextExtraction extractedUserContext) {
		// Create and save user context
		UserContextCreateRequest userContextRequest = UserContextCreateRequest.builder()
				.conversationId(sessionId)
				.journey(extractedUserContext.getJourney())
				.loveTypes(extractedUserContext.getLoveTypes())
				.relationalNeeds(extractedUserContext.getRelationalNeeds())
				.relationshipStatus(extractedUserContext.getRelationshipStatus())
				.semanticSummary(extractedUserContext.getSemanticSummary())
				.build();

		UserContextDTO savedUserContext = userContextService.create(userId, userContextRequest);
		log.info("User context saved successfully sessionId={} userContextId={}", sessionId, savedUserContext.getId());

		return savedUserContext;
	}

	/**
	 * Get ritual pack recommendation from recommendation engine.
	 */
	private RitualPackDTO getRitualPackRecommendation(UserContextDTO savedUserContext) {
		UUID sessionId = savedUserContext.getConversationId();
		RitualPackDTO recommendedPack = recommendationEngine.recommendRitualPack(savedUserContext)
				.orElse(null);

		if (recommendedPack == null) {
			log.info("No ritual pack could be recommended for this session sessionId={}", sessionId);
		} else {
			log.info("Ritual pack recommended sessionId={} ritualPackId={}", sessionId, recommendedPack.getId());
		}

		return recommendedPack;
	}
}
