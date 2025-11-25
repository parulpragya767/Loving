package com.lovingapp.loving.model.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.lovingapp.loving.model.enums.RecommendationSource;
import com.lovingapp.loving.model.enums.RecommendationStatus;
import com.lovingapp.loving.model.enums.RitualHistoryStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public final class RitualRecommendationDTOs {

    private RitualRecommendationDTOs() {
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class RitualRecommendationDTO {
        @NotNull
        private UUID id;
        @NotNull
        private UUID userId;
        @NotNull
        private RecommendationSource source;
        @NotNull
        private UUID sourceId;
        @NotNull
        private UUID ritualPackId;
        @NotNull
        private RecommendationStatus status;
        private OffsetDateTime createdAt;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class RitualRecommendationUpdateRequest {
        private RecommendationStatus status;
        private List<RitualStatusUpdate> ritualStatusUpdates;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class RitualStatusUpdate {
        @NotNull
        private UUID ritualId;
        @NotNull
        private RitualHistoryStatus status;
    }
}
