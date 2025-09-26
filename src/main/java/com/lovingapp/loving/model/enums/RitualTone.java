package com.lovingapp.loving.model.enums;

public enum RitualTone {
    ROMANTIC("Romantic"),
    PLAYFUL("Playful"),
    SERIOUS("Serious"),
    LIGHTHEARTED("Lighthearted"),
    INTIMATE("Intimate"),
    ADVENTUROUS("Adventurous"),
    CALM("Calm"),
    ENERGETIC("Energetic"),
    THOUGHTFUL("Thoughtful"),
    SPONTANEOUS("Spontaneous");

    private final String displayName;

    RitualTone(String displayName) {
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
