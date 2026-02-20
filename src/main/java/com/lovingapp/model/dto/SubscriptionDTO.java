package com.lovingapp.model.dto;

import java.time.OffsetDateTime;

import com.lovingapp.model.enums.SubscriptionStatus;
import com.lovingapp.model.enums.SubscriptionTier;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDTO {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private SubscriptionTier tier;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private SubscriptionStatus status;

    private OffsetDateTime expiresAt;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isBetaUser;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean hasAccess;
}
