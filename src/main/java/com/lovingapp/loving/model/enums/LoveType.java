package com.lovingapp.loving.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true)
public enum LoveType {
        BELONG("Belong", "Feeling seen, understood, and accepted — the love of mutual understanding and emotional safety."),
        FIRE("Fire", "Passionate, embodied love — the energy of desire, vitality, and physical connection."),
        SPARK("Spark",
                        "Playful attraction and curiosity — the first glimmers of excitement, discovery, and aliveness between two people."),
        CARE("Care", "Gentle, nurturing love — tending to each other’s needs with warmth, patience, and kindness."),
        SELF("Self", "Rooted self-love — awareness, compassion, and respect for one’s own needs and truth."),
        BUILD("Build",
                        "Practical, committed love — the steady partnership that grows through shared effort, trust, and reliability."),
        GROW("Grow", "Evolving love — encouraging each other’s learning, transformation, and becoming over time."),
        BEYOND("Beyond",
                        "Transcendent love — giving and receiving freely, connecting to something larger than oneself or the relationship."),
        GRACE("Grace",
                        "Forgiving, compassionate love — the quiet strength that softens conflict and makes repair possible.");

        private final String displayName;
        private final String description;
}
