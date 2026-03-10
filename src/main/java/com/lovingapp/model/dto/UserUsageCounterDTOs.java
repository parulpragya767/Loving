package com.lovingapp.model.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.lovingapp.model.enums.UsagePeriodType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public final class UserUsageCounterDTOs {

    private UserUsageCounterDTOs() {
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class UserUsageCounterDTO {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private UUID id;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private UsagePeriodType periodType;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private OffsetDateTime periodStart;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer aiMessagesCount;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer recommendationsCount;

        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsageQuotaDTO {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer aiMessagesRemainingToday;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer recommendationsRemainingThisWeek;
    }
}
