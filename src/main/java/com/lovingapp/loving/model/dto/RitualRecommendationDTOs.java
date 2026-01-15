package com.lovingapp.loving.model.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.lovingapp.loving.model.enums.RecommendationSource;
import com.lovingapp.loving.model.enums.RecommendationStatus;
import com.lovingapp.loving.model.enums.RitualHistoryStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
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
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private UUID id;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private UUID userId;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private RecommendationSource source;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private UUID sourceId;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private UUID ritualPackId;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private RecommendationStatus status;

        private OffsetDateTime createdAt;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class RitualRecommendationCreateRequest {
        @NotNull(message = "source is required")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private RecommendationSource source;

        @NotNull(message = "sourceId is required")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private UUID sourceId;

        @NotNull(message = "ritualPackId is required")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private UUID ritualPackId;

        @NotNull(message = "status is required")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private RecommendationStatus status;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class RitualRecommendationUpdateRequest {
        @NotNull(message = "status is required")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private RecommendationStatus status;

        @Valid
        private List<RitualStatusUpdate> ritualStatusUpdates;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class RitualStatusUpdate {
        @NotNull(message = "ritualId is required")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private UUID ritualId;

        @NotNull(message = "status is required")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private RitualHistoryStatus status;
    }
}
