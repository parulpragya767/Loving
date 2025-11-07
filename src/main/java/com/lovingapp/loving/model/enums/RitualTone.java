package com.lovingapp.loving.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true)
public enum RitualTone {
    WARM("Warm", "Gentle, affectionate, emotionally open tone that evokes comfort and care."),
    PLAYFUL("Playful", "Light, fun, and humorous tone that invites laughter and spontaneity."),
    INTIMATE("Intimate", "Tender, emotionally or physically close tone that fosters closeness and vulnerability."),
    REFLECTIVE("Reflective", "Thoughtful and sincere tone that encourages deeper understanding or insight."),
    CALM("Calm", "Grounded, soothing tone that brings a sense of peace, slowness, or stillness."),
    ADVENTUROUS("Adventurous", "Curious and bold tone that invites exploration, novelty, or shared discovery."),
    ENERGETIC("Energetic", "Lively and dynamic tone that inspires movement, excitement, or enthusiasm."),
    HEALING("Healing", "Gentle, restorative tone that supports emotional repair, forgiveness, or tenderness."),
    SACRED("Sacred", "Reverent or soulful tone that evokes meaning, depth, or connection beyond the everyday.");

    private final String displayName;
    private final String description;

    @Override
    public String toString() {
        return displayName;
    }
}
