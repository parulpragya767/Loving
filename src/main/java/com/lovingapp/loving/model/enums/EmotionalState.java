package com.lovingapp.loving.model.enums;

public enum EmotionalState {
    HAPPY("Happy"),
    SAD("Sad"),
    ANXIOUS("Anxious"),
    PEACEFUL("Peaceful"),
    EXCITED("Excited"),
    TIRED("Tired"),
    STRESSED("Stressed"),
    LOVING("Loving"),
    GRATEFUL("Grateful"),
    FRUSTRATED("Frustrated"),
    OVERWHELMED("Overwhelmed");

    private final String displayName;

    EmotionalState(String displayName) {
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
