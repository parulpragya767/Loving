package com.lovingapp.loving.model.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.lovingapp.loving.model.enums.ChatMessageRole;
import com.lovingapp.loving.model.enums.ChatSessionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Objects for AI chat functionality.
 */
public class ChatDTOs {

    /**
     * Request to start a new chat session or continue an existing one.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StartSessionRequest {
        private UUID userId;
        private UUID sessionId;
        private String conversationTitle; // optional if you want to thread with frontend id
    }

    /**
     * Response containing session details after starting or continuing a chat
     * session.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StartSessionResponse {
        private UUID sessionId;
        private String conversationTitle;
        private List<ChatMessageDTO> messages;
    }

    /**
     * Request to send a message in a chat session.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SendMessageRequest {
        private String content; // user message
        private boolean isReadyForRitualSuggestion;
    }

    /**
     * Represents a chat message in the system.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatMessageDTO {
        private UUID id;
        private UUID sessionId;
        private ChatMessageRole role;
        private String content;
        private OffsetDateTime createdAt;
    }

    /**
     * Response containing the assistant's reply to a user message.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SendMessageResponse {
        private ChatMessageDTO assistantMessage;
        private boolean isReadyForRitualSuggestion;
        private RitualPackDTO recommendedRitualPack;
    }

    /**
     * Response containing the chat history for a session.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GetHistoryResponse {
        private UUID sessionId;
        private List<ChatMessageDTO> messages;
    }

    /**
     * Response containing sample prompts for the user.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SamplePromptsResponse {
        private List<String> prompts;
    }

    /**
     * Summary information about a chat session, suitable for listing.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionSummaryDTO {
        private UUID id;
        private String conversationTitle;
        private ChatSessionStatus status;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }

    /**
     * Paginated response for listing chat sessions for a user.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListSessionsResponse {
        private List<SessionSummaryDTO> sessions;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
    }
}
