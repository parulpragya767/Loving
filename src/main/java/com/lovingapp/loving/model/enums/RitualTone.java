package com.lovingapp.loving.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true)
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

    @Override
    public String toString() {
        return displayName;
    }
}
