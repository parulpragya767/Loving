package com.lovingapp.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true)
public enum LoveType {
        BELONG("Belong", "Feeling deeply seen, understood, and emotionally safe with each other."),
        FIRE("Fire", "Passion, desire, and embodied physical aliveness between you."),
        SPARK("Spark", "Playful attraction, curiosity, and shared excitement."),
        CARE("Care", "Nurturing each other's needs with warmth, patience, and steady attentiveness."),
        SELF("Self", "Honoring your own needs, boundaries, and inner truth within love."),
        BUILD("Build", "Strengthening commitment through shared responsibility, reliability, and daily partnership."),
        GROW("Grow", "Supporting each other's learning, change, and becoming over time."),
        BEYOND("Beyond", "Creating shared meaning, purpose, or contribution that extends beyond yourselves."),
        GRACE("Grace", "Offering appreciation, forgiveness, and compassion, especially during repair.");

        private final String displayName;
        private final String description;
}
