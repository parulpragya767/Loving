package com.lovingapp.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.mapper.ChatMessageMapper;
import com.lovingapp.mapper.ChatSessionMapper;
import com.lovingapp.model.domain.RitualRecommendationContext;
import com.lovingapp.model.domain.ai.LLMEmpatheticResponse;
import com.lovingapp.model.domain.ai.LLMUserContextExtraction;
import com.lovingapp.model.dto.ChatDTOs.ChatSessionDTO;
import com.lovingapp.model.dto.ChatDTOs.RecommendRitualPackResponse;
import com.lovingapp.model.dto.ChatDTOs.SendMessageRequest;
import com.lovingapp.model.dto.ChatDTOs.SendMessageResponse;
import com.lovingapp.model.dto.RitualPackDTO;
import com.lovingapp.model.dto.UserContextDTOs.UserContextCreateRequest;
import com.lovingapp.model.dto.UserContextDTOs.UserContextDTO;
import com.lovingapp.model.entity.ChatMessage;
import com.lovingapp.model.entity.ChatSession;
import com.lovingapp.model.enums.ChatMessageRole;
import com.lovingapp.model.enums.FeatureType;
import com.lovingapp.service.chat.AIChatLLMHelper;
import com.lovingapp.service.chat.AIChatMessagePersistenceService;
import com.lovingapp.service.chat.AIChatRitualRecommendationAndHistoryHelper;
import com.lovingapp.service.chat.AIChatSessionPersistenceService;

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
	private final RitualRecommendationService ritualRecommendationService;
	private final AIChatLLMHelper aiChatLLMHelper;
	private final AIChatRitualRecommendationAndHistoryHelper ritualRecommendationAndHistoryHelper;
	private final FeatureAccessService featureAccessService;
	private final UsageService usageService;

	@Transactional
	public ChatSessionDTO createSession(UUID userId) {
		return chatSessionPersistenceService.createSession(userId);
	}

	@Transactional
	public SendMessageResponse sendMessage(UUID userId, UUID sessionId, SendMessageRequest request) {
		// Check if user has remaining quota for AI chat
		featureAccessService.assertAccess(userId, FeatureType.AI_CHAT);

		// 0. Validate session exists and belongs to the user
		ChatSession session = chatSessionPersistenceService.findSessionByIdAndUserId(sessionId, userId);

		// 1. Save user message
		chatMessagePersistenceService.saveUserMessage(sessionId, request.getContent());

		// 2. Get conversation history
		List<ChatMessage> messages = buildLlmConversationContext(userId, sessionId);

		// 3. Generate empathetic response from the conversation history using LLM
		LLMEmpatheticResponse empatheticResponse = aiChatLLMHelper.generateEmpatheticResponse(sessionId, messages);
		String response = empatheticResponse.getResponse();
		boolean ready = empatheticResponse.isReadyForRitualSuggestion();

		// 4. Create and save assistant message
		ChatMessage savedAssistantMessage = chatMessagePersistenceService.saveAssistantMessage(sessionId, response);
		log.info("Assistant message created sessionId={} chatMessageId={} readyForRecommendation={}", sessionId,
				savedAssistantMessage.getId(), ready);

		chatSessionPersistenceService.updateSessionTitleAndLastMessagePreview(session, null, response);

		// Increment AI message usage counter
		usageService.incrementAiMessageUsage(userId);

		return SendMessageResponse.builder()
				.assistantResponse(ChatMessageMapper.toDto(savedAssistantMessage))
				.isReadyForRitualPackRecommendation(ready)
				.build();
	}

	@Transactional
	public RecommendRitualPackResponse recommendRitualPack(UUID userId, UUID sessionId) {
		// Check if user has remaining quota for Ritual Pack Recommendation
		featureAccessService.assertAccess(userId, FeatureType.RITUAL_PACK_RECOMMENDATION);

		// Validate session exists and belongs to user and fetch chat messages
		ChatSession session = chatSessionPersistenceService.findSessionByIdAndUserId(sessionId, userId);

		List<ChatMessage> messages = buildLlmConversationContext(userId, sessionId);

		// Extract user context from conversation using LLM
		LLMUserContextExtraction extractedUserContext = aiChatLLMHelper.extractUserContext(userId, sessionId, messages);

		// Save user context
		UserContextDTO savedUserContext = saveUserContext(userId, sessionId, extractedUserContext);

		// Get ritual pack recommendation
		RitualPackDTO recommendedPack = getRitualPackRecommendation(userId, savedUserContext);

		// Generate wrap-up message using LLM
		String wrapUpMessage = aiChatLLMHelper.generateWrapUpMessage(messages, recommendedPack, sessionId);

		// Save wrap-up message
		ChatMessage savedAssistantMessage = chatMessagePersistenceService.saveWrapUpMessage(sessionId, wrapUpMessage);

		// Create recommendation and history records
		UUID recommendationId = ritualRecommendationAndHistoryHelper.createRecommendationAndHistory(
				userId, sessionId, recommendedPack);

		// Update session title and lastMessagePreview
		chatSessionPersistenceService.updateSessionTitleAndLastMessagePreview(session,
				extractedUserContext.getConversationTitle(), "✨ Ritual pack suggested");

		// Increment ritual pack recommendation counter
		usageService.incrementRecommendationUsage(userId);

		return RecommendRitualPackResponse.builder()
				.ritualPack(recommendedPack)
				.recommendationId(recommendationId)
				.wrapUpResponse(ChatMessageMapper.toDto(savedAssistantMessage))
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
				"We've been busy and a bit out of sync lately.",
				"Daily life has been getting in the way of us a bit.",
				"Things feel a little flat between us lately.");
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
	 * Build conversation context for LLM by appending all semantic summaries found
	 * for this conversation and the current messages.
	 */
	private List<ChatMessage> buildLlmConversationContext(UUID userId, UUID sessionId) {
		List<ChatMessage> allMessages = chatMessagePersistenceService.findMessagesBySessionId(sessionId);

		// Find last recommendation system message
		int lastRecommendationIndex = IntStream.range(0, allMessages.size())
				.filter(i -> {
					ChatMessage m = allMessages.get(i);
					return m.getRole() == ChatMessageRole.SYSTEM
							&& m.getMetadata() != null
							&& m.getMetadata().getRecommendationId() != null;
				})
				.max()
				.orElse(-1);

		List<ChatMessage> relevantMessages = lastRecommendationIndex >= 0
				? allMessages.subList(lastRecommendationIndex + 1, allMessages.size())
				: allMessages;

		List<ChatMessage> semanticSummaries = userContextService.findByConversationId(userId, sessionId).stream()
				.map(UserContextDTO::getSemanticSummary)
				.filter(summary -> summary != null && !summary.trim().isEmpty())
				.map(summary -> ChatMessage.builder()
						.sessionId(sessionId)
						.role(ChatMessageRole.SYSTEM)
						.content("Semantic summary of earlier conversation context:\n" + summary)
						.build())
				.toList();

		List<ChatMessage> llmContext = new ArrayList<>();
		llmContext.addAll(semanticSummaries);
		llmContext.addAll(relevantMessages);

		return llmContext;
	}

	/**
	 * Get ritual pack recommendation from recommendation engine.
	 */
	private RitualPackDTO getRitualPackRecommendation(UUID userId, UserContextDTO savedUserContext) {
		UUID sessionId = savedUserContext.getConversationId();

		// Fetch recommendation history
		List<RitualRecommendationContext.RitualPackRecommendationEvent> recommendationHistory = ritualRecommendationService
				.getAll(userId)
				.stream()
				.map(recommendation -> RitualRecommendationContext.RitualPackRecommendationEvent.builder()
						.ritualPackId(recommendation.getRitualPackId())
						.recommendedAt(recommendation.getCreatedAt())
						.build())
				.collect(Collectors.toList());

		// Build recommendation context
		RitualRecommendationContext context = RitualRecommendationContext.builder()
				.journey(savedUserContext.getJourney())
				.loveTypes(savedUserContext.getLoveTypes())
				.relationalNeeds(savedUserContext.getRelationalNeeds())
				.relationshipStatus(savedUserContext.getRelationshipStatus())
				.semanticSummary(savedUserContext.getSemanticSummary())
				.recommendationHistory(recommendationHistory)
				.build();

		// Get recommendations (limit to 1 for now)
		List<RitualPackDTO> recommendedPacks = recommendationEngine.recommend(context, 1);

		RitualPackDTO recommendedPack = recommendedPacks.isEmpty() ? null : recommendedPacks.get(0);

		if (recommendedPack == null) {
			log.info("No ritual pack could be recommended for this session sessionId={}", sessionId);
		} else {
			log.info("Ritual pack recommended sessionId={} ritualPackId={}", sessionId, recommendedPack.getId());
		}

		return recommendedPack;
	}
}
