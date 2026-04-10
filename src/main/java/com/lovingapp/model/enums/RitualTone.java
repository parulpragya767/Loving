package com.lovingapp.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true)
public enum RitualTone {
    WARM("Warm", "Gentle, affectionate, emotionally open."),
    PLAYFUL("Playful", "Light, fun, teasing or spontaneous."),
    INTIMATE("Intimate", "Tender, close, vulnerable."),
    REFLECTIVE("Reflective", "Thoughtful, sincere, inward-looking."),
    CALM("Calm", "Grounded, steady, soothing."),
    ADVENTUROUS("Adventurous", "Curious, bold, exploratory."),
    ENERGETIC("Energetic", "Lively, dynamic, activating."),
    HEALING("Healing", "Soft, restorative, repairing."),
    SACRED("Sacred", "Reverent, meaningful, quietly profound.");

    private final String displayName;
    private final String description;
}
