package com.lovingapp.loving.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true)
public enum LifeContext {
    MORNING_ROUTINE("Morning Routine"),
    EVENING_ROUTINE("Evening Routine"),
    WEEKEND("Weekend"),
    HOLIDAY("Holiday"),
    ANNIVERSARY("Anniversary"),
    DATE_NIGHT("Date Night"),
    LONG_DISTANCE("Long Distance"),
    STRESSFUL_PERIOD("Stressful Period"),
    NEW_RELATIONSHIP("New Relationship"),
    LONG_TERM_RELATIONSHIP("Long-term Relationship");

    private final String displayName;

    @Override
    public String toString() {
        return displayName;
    }
}
