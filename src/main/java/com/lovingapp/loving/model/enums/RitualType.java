package com.lovingapp.loving.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true)
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

    @Override
    public String toString() {
        return displayName;
    }
}
