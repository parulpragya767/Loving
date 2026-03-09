package com.lovingapp.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum UsagePeriodType {
    DAILY,
    WEEKLY
}
