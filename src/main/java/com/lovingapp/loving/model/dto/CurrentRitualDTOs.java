package com.lovingapp.loving.model.dto;

import java.util.List;
import java.util.UUID;

import com.lovingapp.loving.model.enums.RitualHistoryStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public final class CurrentRitualDTOs {

    private CurrentRitualDTOs() {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentRitualsDTO {
        private List<CurrentRitualPackDTO> ritualPacks;
        private List<CurrentRitualDTO> individualRituals;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentRitualPackDTO {
        private UUID ritualPackId;
        private UUID recommendationId;
        private RitualPackDTO ritualPack;
        private List<CurrentRitualDTO> rituals;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentRitualDTO {
        private UUID ritualHistoryId;
        private UUID ritualId;
        private RitualDTO ritual;
        private RitualHistoryStatus status;
    }
}
