package com.lovingapp.loving.model.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.lovingapp.loving.model.enums.Journey;
import com.lovingapp.loving.model.enums.LoveType;
import com.lovingapp.loving.model.enums.RelationalNeed;
import com.lovingapp.loving.model.enums.RelationshipStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public final class UserContextDTOs {
    private UserContextDTOs() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserContextDTO {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private UUID id;

        private UUID conversationId;
        private Journey journey;
        private List<LoveType> loveTypes;
        private List<RelationalNeed> relationalNeeds;
        private RelationshipStatus relationshipStatus;
        private String semanticSummary;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserContextCreateRequest {
        private UUID conversationId;
        private Journey journey;
        private List<LoveType> loveTypes;
        private List<RelationalNeed> relationalNeeds;
        private RelationshipStatus relationshipStatus;
        private String semanticSummary;
    }
}
