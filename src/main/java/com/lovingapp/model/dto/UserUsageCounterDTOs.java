package com.lovingapp.model.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.lovingapp.model.enums.UsagePeriodType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class UserUsageCounterCreateRequest {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "periodType is required")
        private UsagePeriodType periodType;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "periodStart is required")
        private OffsetDateTime periodStart;

        @Builder.Default
        private Integer aiMessagesCount = 0;

        @Builder.Default
        private Integer recommendationsCount = 0;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class UserUsageCounterIncrementRequest {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "periodType is required")
        private UsagePeriodType periodType;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "periodStart is required")
        private OffsetDateTime periodStart;
    }
}
