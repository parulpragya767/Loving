package com.lovingapp.loving.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum RitualHistoryStatus {
    SUGGESTED,
    ACTIVE,
    STARTED,
    COMPLETED,
    SKIPPED,
    ABANDONED
}
