package com.lovingapp.loving.model.dto;

import java.util.List;
import java.util.UUID;

import com.lovingapp.loving.model.dto.RitualHistoryDTOs.RitualHistoryDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public final class UserRitualsDTOs {

    private UserRitualsDTOs() {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentRitualsDTO {
        private List<UserRitualPackDTO> ritualPacks;
        private List<UserRitualDTO> individualRituals;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRitualPackDTO {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private UUID ritualPackId;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private UUID recommendationId;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private RitualPackDTO ritualPack;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private List<UserRitualDTO> rituals;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRitualDTO {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private UUID ritualHistoryId;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private UUID ritualId;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private RitualDTO ritual;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private RitualHistoryDTO ritualHistory;
    }
}
