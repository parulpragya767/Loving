package com.lovingapp.loving.model.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.lovingapp.loving.model.enums.RecommendationSource;
import com.lovingapp.loving.model.enums.RecommendationStatus;

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
        private UUID id;
        private UUID userId;
        private RecommendationSource source;
        private UUID sourceId;
        private UUID ritualPackId;
        private RecommendationStatus status;
        private OffsetDateTime createdAt;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class UpdateStatusRequest {
        @NotNull
        private RecommendationStatus status;
    }
}
