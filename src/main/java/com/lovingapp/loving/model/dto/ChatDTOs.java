package com.lovingapp.loving.model.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.lovingapp.loving.model.domain.ChatMetadata;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.RitualHistoryDTO;
import com.lovingapp.loving.model.enums.ChatMessageRole;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Objects for AI chat functionality.
 */
public class ChatDTOs {

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
        private ChatMetadata metadata;
        private OffsetDateTime createdAt;
    }

    /**
     * Represents a chat session in the system.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatSessionDTO {
        @NotNull
        private UUID id;
        private String title;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
        private List<ChatMessageDTO> messages; // optional, nullable
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
    }

    /**
     * Response containing the assistant's reply to a user message and readiness for
     * ritual pack recommendation.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SendMessageResponse {
        private ChatMessageDTO assistantResponse;
        private boolean isReadyForRitualPackRecommendation;
    }

    /**
     * Response containing the recommended ritual pack and wrap-up message.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecommendRitualPackResponse {
        private RitualPackDTO ritualPack;
        private ChatMessageDTO wrapUpResponse;
        private Map<UUID, RitualHistoryDTO> ritualHistoryMap;
    }
}
