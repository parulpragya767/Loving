package com.lovingapp.loving.model.enums;

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

    LifeContext(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
