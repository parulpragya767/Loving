package com.lovingapp.loving.model.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.lovingapp.loving.model.enums.RitualFeedback;
import com.lovingapp.loving.model.enums.RitualHistoryStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public final class RitualHistoryDTOs {

    private RitualHistoryDTOs() {
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class RitualHistoryDTO {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private UUID id;
        private UUID userId;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private UUID ritualId;
        private UUID ritualPackId;
        private UUID recommendationId;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private RitualHistoryStatus status;
        private RitualFeedback feedback;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class RitualHistoryCreateRequest {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "ritualId is required")
        private UUID ritualId;

        private UUID ritualPackId;

        private UUID recommendationId;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "status is required")
        private RitualHistoryStatus status;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class RitualHistoryUpdateRequest {
        @NotNull(message = "status is required")
        private RitualHistoryStatus status;

        private RitualFeedback feedback;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class BulkRitualHistoryStatusUpdateRequest {
        @NotNull(message = "updates is required")
        @Valid
        private List<StatusUpdateEntry> updates;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class StatusUpdateEntry {
        @NotNull(message = "ritualHistoryId is required")
        private UUID ritualHistoryId;

        @NotNull(message = "status is required")
        private RitualHistoryStatus status;
    }
}
