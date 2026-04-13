package com.lovingapp.service.chat;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.model.dto.RitualPackDTO;
import com.lovingapp.model.entity.ChatMessage;
import com.lovingapp.model.entity.ChatSession;

import lombok.RequiredArgsConstructor;

/**
 * Helper for handling transactional operations in AI chat.
 */
@Service
@RequiredArgsConstructor
public class AIChatTransactionHelper {

	private final AIChatMessagePersistenceService chatMessagePersistenceService;
	private final AIChatSessionPersistenceService chatSessionPersistenceService;
	private final AIChatRitualRecommendationAndHistoryHelper ritualRecommendationAndHistoryHelper;

	/**
	 * Record to hold the result of saving recommendation data.
	 */
	public record RecommendationResult(ChatMessage assistantMessage, UUID recommendationId) {
	}

	/**
	 * Save assistant message and update session in a single transaction.
	 */
	@Transactional
	public ChatMessage saveAssistantMessageAndUpdateSession(UUID sessionId, ChatSession session, String response) {
		ChatMessage savedAssistantMessage = chatMessagePersistenceService.saveAssistantMessage(sessionId, response);
		chatSessionPersistenceService.updateSessionTitleAndLastMessagePreview(session, null, response);
		return savedAssistantMessage;
	}

	/**
	 * Save all recommendation-related data in a single transaction.
	 */
	@Transactional
	public RecommendationResult saveRecommendationDataInTransaction(
			UUID userId, UUID sessionId, ChatSession session, String wrapUpMessage,
			RitualPackDTO recommendedPack, String conversationTitle) {
		// Save wrap-up message
		ChatMessage savedAssistantMessage = chatMessagePersistenceService.saveWrapUpMessage(sessionId, wrapUpMessage);

		// Create recommendation and history records
		UUID recommendationId = ritualRecommendationAndHistoryHelper.createRecommendationAndHistory(
				userId, sessionId, recommendedPack);

		// Update session title and lastMessagePreview
		chatSessionPersistenceService.updateSessionTitleAndLastMessagePreview(session,
				conversationTitle, "✨ Ritual pack suggested");

		return new RecommendationResult(savedAssistantMessage, recommendationId);
	}
}
