package com.lovingapp.loving.model.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.lovingapp.loving.model.enums.EmojiFeedback;
import com.lovingapp.loving.model.enums.RitualHistoryStatus;

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
        private UUID id;
        private UUID userId;
        private UUID ritualId;
        private UUID ritualPackId;
        private RitualHistoryStatus status;
        private EmojiFeedback feedback;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }

    @Data
    public static class RitualHistoryUpdateRequest {
        private RitualHistoryStatus status;
        private EmojiFeedback feedback;
    }

    @Data
    public static class BulkRitualHistoryStatusUpdateRequest {
        @NotNull
        @Valid
        private List<StatusUpdateEntry> updates;
    }

    @Data
    public static class StatusUpdateEntry {
        @NotNull
        private UUID ritualHistoryId;

        @NotNull
        private RitualHistoryStatus status;
    }
}
