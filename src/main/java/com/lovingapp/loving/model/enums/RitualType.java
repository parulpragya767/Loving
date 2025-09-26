package com.lovingapp.loving.model.enums;

public enum RitualType {
    REFLECTION("Reflection"),
    CONVERSATION("Conversation"),
    PLAY("Play"),
    APPRECIATION("Appreciation"),
    GRATITUDE("Gratitude"),
    PHYSICAL_CONNECTION("Physical Connection"),
    QUALITY_TIME("Quality Time"),
    ADVENTURE("Adventure"),
    RELAXATION("Relaxation"),
    SELF_IMPROVEMENT("Self-Improvement");

    private final String displayName;

    RitualType(String displayName) {
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
