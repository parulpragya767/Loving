package com.lovingapp.loving.dto.ai;

import com.lovingapp.loving.model.ai.ChatMessageRole;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class ChatDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StartSessionRequest {
        private UUID userId;
        private String conversationId; // optional if you want to thread with frontend id
        private String systemPrompt;   // optional custom system prompt
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StartSessionResponse {
        private UUID sessionId;
        private UUID userId;
        private String conversationId;
        private OffsetDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SendMessageRequest {
        private String content; // user message
    }

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SendMessageResponse {
        private ChatMessageDTO userMessage;
        private ChatMessageDTO assistantMessage;
        private boolean askedFollowUp;
        private boolean recommendationTriggered;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HistoryResponse {
        private UUID sessionId;
        private List<ChatMessageDTO> messages;
    }
}
