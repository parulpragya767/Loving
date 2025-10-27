package com.lovingapp.loving.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum Rhythm {
    DAILY,
    WEEKLY,
    OCCASIONAL,
    EVENT_TRIGGERED
}
