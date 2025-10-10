package com.lovingapp.loving.service.ai;

import java.util.UUID;

import com.lovingapp.loving.model.dto.ai.ChatDTOs;

import reactor.core.publisher.Mono;

/**
 * Service interface for managing AI chat sessions and messages.
 */
public interface AIChatService {

    /**
     * Start a new chat session or continue an existing one.
     * 
     * @param request The start session request containing user ID and optional
     *                conversation ID
     * @return A Mono emitting the start session response
     */
    Mono<ChatDTOs.StartSessionResponse> startSession(ChatDTOs.StartSessionRequest request);

    /**
     * Send a message in an existing chat session.
     * 
     * @param sessionId The ID of the chat session
     * @param request   The message request containing the user's message
     * @return A Mono emitting the send message response with the assistant's reply
     */
    Mono<ChatDTOs.SendMessageResponse> sendMessage(UUID sessionId, ChatDTOs.SendMessageRequest request);

    /**
     * Get the chat history for a session.
     * 
     * @param sessionId The ID of the chat session
     * @return A Mono emitting the chat history response with all messages
     */
    Mono<ChatDTOs.GetHistoryResponse> getChatHistory(UUID sessionId);
}
