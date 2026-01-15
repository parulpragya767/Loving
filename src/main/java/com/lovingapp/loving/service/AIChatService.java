package com.lovingapp.loving.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.loving.client.LlmClient;
import com.lovingapp.loving.exception.ResourceNotFoundException;
import com.lovingapp.loving.helpers.ai.LLMPromptHelper;
import com.lovingapp.loving.mapper.ChatMessageMapper;
import com.lovingapp.loving.mapper.ChatSessionMapper;
import com.lovingapp.loving.model.domain.ChatMetadata;
import com.lovingapp.loving.model.domain.ai.LLMChatMessage;
import com.lovingapp.loving.model.domain.ai.LLMEmpatheticResponse;
import com.lovingapp.loving.model.domain.ai.LLMRequest;
import com.lovingapp.loving.model.domain.ai.LLMResponse;
import com.lovingapp.loving.model.domain.ai.LLMResponseFormat;
import com.lovingapp.loving.model.domain.ai.LLMUserContextExtraction;
import com.lovingapp.loving.model.dto.ChatDTOs.ChatSessionDTO;
import com.lovingapp.loving.model.dto.ChatDTOs.RecommendRitualPackResponse;
import com.lovingapp.loving.model.dto.ChatDTOs.SendMessageRequest;
import com.lovingapp.loving.model.dto.ChatDTOs.SendMessageResponse;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.RitualHistoryCreateRequest;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.RitualHistoryDTO;
import com.lovingapp.loving.model.dto.RitualPackDTO;
import com.lovingapp.loving.model.dto.RitualRecommendationDTOs.RitualRecommendationCreateRequest;
import com.lovingapp.loving.model.dto.RitualRecommendationDTOs.RitualRecommendationDTO;
import com.lovingapp.loving.model.dto.UserContextDTOs.UserContextCreateRequest;
import com.lovingapp.loving.model.dto.UserContextDTOs.UserContextDTO;
import com.lovingapp.loving.model.entity.ChatMessage;
import com.lovingapp.loving.model.entity.ChatSession;
import com.lovingapp.loving.model.enums.ChatMessageRole;
import com.lovingapp.loving.model.enums.RecommendationSource;
import com.lovingapp.loving.model.enums.RecommendationStatus;
import com.lovingapp.loving.model.enums.RitualHistoryStatus;
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
	private final RecommendationEngine recommendationEngine;
	private final RitualRecommendationService ritualRecommendationService;
	private final RitualHistoryService ritualHistoryService;

	@Transactional
	public ChatSessionDTO createSession(UUID userId) {
		ChatSession session = ChatSession.builder()
				.userId(userId)
				.build();
		ChatSession saved = chatSessionRepository.saveAndFlush(session);
		return ChatSessionMapper.toDto(saved);
	}

	@Transactional
	public SendMessageResponse sendMessage(UUID userId, UUID sessionId, SendMessageRequest request) {
		// 0. Validate session exists and belongs to the user
		chatSessionRepository.findByIdAndUserId(sessionId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("ChatSession", "id", sessionId));

		// 1. Save user message
		ChatMessage userMessage = ChatMessage.builder()
				.sessionId(sessionId)
				.role(ChatMessageRole.USER)
				.content(request.getContent())
				.build();

		ChatMessage savedUserMessage = chatMessageRepository.saveAndFlush(userMessage);
		log.info("User chat message saved successfully sessionId={} chatMessageId={}", sessionId,
				savedUserMessage.getId());

		// 2. Get conversation history
		List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);

		// 3. Generate empathetic response from the conversation history using LLM
		LLMEmpatheticResponse empatheticResponse = generateEmpatheticResponse(sessionId, messages);
		String response = empatheticResponse.getResponse();
		boolean ready = empatheticResponse.isReadyForRitualSuggestion();

		// 4. Create and save assistant message
		ChatMessage assistantMessage = ChatMessage.builder()
				.sessionId(sessionId)
				.role(ChatMessageRole.ASSISTANT)
				.content(response)
				.build();
		ChatMessage savedAssistantMessage = chatMessageRepository.saveAndFlush(assistantMessage);
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
		ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("ChatSession", "id", sessionId));

		List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);

		// Extract user context from conversation using LLM
		LLMUserContextExtraction extractedUserContext = extractUserContext(userId, sessionId, messages);

		// Save user context
		UserContextDTO savedUserContext = saveUserContext(userId, sessionId, extractedUserContext);

		// Update session title if needed
		updateSessionTitle(session, extractedUserContext.getConversationTitle());

		// Get ritual pack recommendation
		RitualPackDTO recommendedPack = getRitualPackRecommendation(savedUserContext);

		// Generate wrap-up message using LLM
		String wrapUpMessage = generateWrapUpMessage(messages, recommendedPack, sessionId);

		// Save wrap-up message
		ChatMessage savedAssistantMessage = saveWrapUpMessage(sessionId, wrapUpMessage);

		// Create recommendation and history records
		List<RitualHistoryDTO> createdHistories = createRecommendationAndHistory(
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
		ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("ChatSession", "id", sessionId));

		List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
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
		return chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
				.map(ChatSessionMapper::toDto)
				.collect(Collectors.toList());
	}

	@Transactional
	public void deleteSession(UUID userId, UUID sessionId) {
		chatSessionRepository.findByIdAndUserId(sessionId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("ChatSession", "id", sessionId));

		// Delete messages first to avoid FK constraints if any
		chatMessageRepository.deleteBySessionId(sessionId);
		// Then delete the session
		chatSessionRepository.deleteById(sessionId);
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

	/**
	 * Generate empathetic response from conversation using LLM.
	 */
	private LLMEmpatheticResponse generateEmpatheticResponse(UUID sessionId, List<ChatMessage> messages) {
		LLMRequest llmRequest = LLMRequest.builder()
				.messages(messages.stream()
						.map(m -> new LLMChatMessage(m.getRole(), m.getContent()))
						.collect(Collectors.toList()))
				.systemPrompt(LLMPromptHelper.generateEmpatheticChatResponsePrompt())
				.responseFormat(LLMResponseFormat.JSON)
				.build();

		// 4. Call LLM to generate empathetic response
		log.info("Generating empathetic response via LLM sessionId={}", sessionId);

		LLMResponse<LLMEmpatheticResponse> aiReply = llmClient.generate(llmRequest, LLMEmpatheticResponse.class);
		LLMEmpatheticResponse empatheticResponse = aiReply.getParsed();

		log.info("Empathetic response via LLM generated successfully sessionId={}", sessionId);

		return empatheticResponse;
	}

	/**
	 * Extract user context from conversation using LLM.
	 */
	private LLMUserContextExtraction extractUserContext(UUID userId, UUID sessionId, List<ChatMessage> messages) {
		LLMRequest extractionRequest = LLMRequest.builder()
				.messages(messages.stream()
						.map(m -> new LLMChatMessage(m.getRole(), m.getContent()))
						.collect(Collectors.toList()))
				.systemPrompt(LLMPromptHelper.generateUserContextExtractionPrompt())
				.responseFormat(LLMResponseFormat.JSON)
				.build();

		log.info("Extracting user context from conversation via LLM sessionId={}", sessionId);

		LLMResponse<LLMUserContextExtraction> llmUserContextResponse = llmClient.generate(extractionRequest,
				LLMUserContextExtraction.class);
		LLMUserContextExtraction llmUserContext = llmUserContextResponse.getParsed();

		log.info("User context extracted successfully via LLM sessionId={}", sessionId);

		return llmUserContext;
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
	 * Update session title if not already set and a title is available from user
	 * context.
	 */
	private void updateSessionTitle(ChatSession session, String conversationTitle) {
		if (conversationTitle != null && !conversationTitle.isBlank()
				&& (session.getTitle() == null || session.getTitle().isBlank())) {
			String suggestedTitle = conversationTitle.trim();
			session.setTitle(suggestedTitle);
			log.info("Session title updated via JPA dirty checking sessionId={}", session.getId());
		}
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

	/**
	 * Generate contextual wrap-up message via LLM that ties the pack to the user's
	 * situation.
	 */
	private String generateWrapUpMessage(List<ChatMessage> messages, RitualPackDTO recommendedPack, UUID sessionId) {
		String wrapUpMessage = "";

		try {
			if (recommendedPack != null) {
				LLMRequest wrapUpRequest = LLMRequest.builder()
						.messages(messages.stream()
								.map(m -> new LLMChatMessage(m.getRole(), m.getContent()))
								.collect(Collectors.toList()))
						.systemPrompt(LLMPromptHelper.generateWrapUpChatResponsePrompt(recommendedPack))
						.responseFormat(LLMResponseFormat.TEXT)
						.build();

				log.info("Generating contextual wrap-up message via LLM sessionId={}", sessionId);

				LLMResponse<String> wrapUpResponse = llmClient.generate(wrapUpRequest);
				wrapUpMessage = wrapUpResponse.getRawText();

				log.info("Contextual wrap-up message generated successfully sessionId={}", sessionId);
			}

			if (wrapUpMessage == null || wrapUpMessage.trim().isEmpty()) {
				wrapUpMessage = getFallbackWrapUpMessage(recommendedPack);
			}
		} catch (Exception ex) {
			log.warn("Ritual wrap-up message LLM generation failed sessionId={}: {}", sessionId, ex.getMessage());
			wrapUpMessage = getFallbackWrapUpMessage(recommendedPack);
		}

		return wrapUpMessage;
	}

	/**
	 * Save the wrap-up message as an assistant message.
	 */
	private ChatMessage saveWrapUpMessage(UUID sessionId, String wrapUpMessage) {
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
	 * Create ritual recommendation and history records if pack was recommended.
	 */
	private List<RitualHistoryDTO> createRecommendationAndHistory(UUID userId, UUID sessionId,
			RitualPackDTO recommendedPack) {
		List<RitualHistoryDTO> createdHistories = new ArrayList<>();

		if (recommendedPack != null) {
			// Create ritual recommendation record
			RitualRecommendationCreateRequest recommendationCreateRequest = RitualRecommendationCreateRequest.builder()
					.source(RecommendationSource.CHAT)
					.sourceId(sessionId)
					.ritualPackId(recommendedPack.getId())
					.status(RecommendationStatus.SUGGESTED)
					.build();
			RitualRecommendationDTO savedRecommendation = ritualRecommendationService.create(userId,
					recommendationCreateRequest);

			log.info("Ritual recommendation saved successfully sessionId={} recommendationId={}", sessionId,
					savedRecommendation.getId());

			// Create and save system chat message with recommendation metadata
			ChatMessage recommendationMessage = ChatMessage.builder()
					.sessionId(sessionId)
					.role(ChatMessageRole.SYSTEM)
					.content("Recommended ritual pack")
					.metadata(ChatMetadata.builder()
							.recommendationId(savedRecommendation.getId())
							.build())
					.build();
			ChatMessage savedRecommendationMessage = chatMessageRepository.saveAndFlush(recommendationMessage);

			log.info(
					"System chat message with recommendation metadata saved successfully sessionId={} chatMessageId={}",
					sessionId,
					savedRecommendationMessage.getId());

			// Bulk create ritual history records for the rituals inside recommended ritual
			// pack
			createdHistories = createRitualHistories(userId, sessionId, recommendedPack,
					savedRecommendation.getId());
		}

		return createdHistories;
	}

	/**
	 * Create ritual history records for the rituals inside the recommended ritual
	 * pack.
	 */
	private List<RitualHistoryDTO> createRitualHistories(UUID userId, UUID sessionId, RitualPackDTO recommendedPack,
			UUID recommendationId) {
		List<UUID> ritualIds = null;
		if (recommendedPack.getRituals() != null && !recommendedPack.getRituals().isEmpty()) {
			ritualIds = recommendedPack.getRituals().stream()
					.map(r -> r.getId())
					.collect(Collectors.toList());
		} else if (recommendedPack.getRitualIds() != null && !recommendedPack.getRitualIds().isEmpty()) {
			ritualIds = recommendedPack.getRitualIds();
		}

		List<RitualHistoryDTO> createdHistories = new ArrayList<>();
		if (ritualIds != null && !ritualIds.isEmpty()) {
			UUID packId = recommendedPack.getId();
			List<RitualHistoryCreateRequest> histories = ritualIds.stream()
					.map(ritualId -> RitualHistoryCreateRequest.builder()
							.ritualId(ritualId)
							.ritualPackId(packId)
							.recommendationId(recommendationId)
							.status(RitualHistoryStatus.SUGGESTED)
							.build())
					.collect(Collectors.toList());
			createdHistories = ritualHistoryService.bulkCreateRitualHistories(userId, histories);

			log.info(
					"Ritual history records created for recommended pack sessionId={} recommendationId={} count={}",
					sessionId, recommendationId, createdHistories.size());
		}

		return createdHistories;
	}
}
