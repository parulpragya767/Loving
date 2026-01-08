package com.lovingapp.loving.model.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public final class UserDTOs {

    private UserDTOs() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserDTO {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private UUID id;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private UUID authUserId;
        private String email;
        private String displayName;
        private Boolean onboardingCompleted;
        private OffsetDateTime lastLoginAt;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserUpdateRequest {
        private String displayName;
        private Boolean onboardingCompleted;
    }
}
