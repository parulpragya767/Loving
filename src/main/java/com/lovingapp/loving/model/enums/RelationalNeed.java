package com.lovingapp.loving.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true)
public enum RelationalNeed {
        CONNECTION("Connection", "Desire to feel emotionally close and present with each other."),
        INTIMACY("Intimacy", "Need for closeness, vulnerability, and affection -- both emotional and physical."),
        UNDERSTANDING("Understanding", "Longing to be truly seen, heard, and empathized with."),
        ACCEPTANCE_AND_FORGIVENESS("Acceptance & Forgiveness",
                        "Need to feel loved without judgment and to repair with compassion."),
        TRUST_AND_SAFETY("Trust & Safety", "Desire for honesty, reliability, and a sense of emotional security."),
        SUPPORT("Support", "Desire for emotional or practical help during stress, change, or challenge."),
        BALANCE_AND_FAIRNESS("Balance & Fairness",
                        "Need for shared responsibility, equality, and mutual respect in daily life."),
        COMMUNICATION("Communication", "Desire for open, honest dialogue and emotional clarity."),
        PLAY_AND_JOY("Play & Joy", "Craving lightness, fun, and shared laughter to refresh the bond."),
        GROWTH("Growth", "Desire to evolve individually and as a couple -- to keep learning and deepening."),
        GRATITUDE_AND_APPRECIATION("Gratitude & Appreciation",
                        "Longing to feel noticed, valued, and thanked for who one is and what one does."),
        PRESENCE_AND_QUALITY_TIME("Presence & Quality Time",
                        "Need to share undistracted, intentional moments together."),
        SPACE("Space", "Need for autonomy, breathing room, and personal reflection within the relationship.");

        private final String displayName;
        private final String description;
}
