package com.lovingapp.loving.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
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

    @Override
    public String toString() {
        return displayName;
    }
}
