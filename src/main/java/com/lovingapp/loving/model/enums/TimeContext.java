package com.lovingapp.loving.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents the time context for when a ritual is intended to be performed.
 */
@Schema(enumAsRef = true)
public enum TimeContext {
    MORNING, // Early day
    AFTERNOON, // Mid-day
    EVENING, // Early night
    NIGHT, // Late night
    WEEKDAY, // Monday-Friday
    WEEKEND, // Saturday-Sunday
    HOLIDAY, // Special occasions and holidays
    ANYTIME // Not time-specific
}
