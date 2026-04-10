package com.lovingapp.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true)
public enum RelationalNeed {
        CONNECTION("Connection", "Feeling emotionally close and bonded."),
        INTIMACY("Intimacy", "Sharing vulnerability and affectionate closeness, emotionally or physically."),
        UNDERSTANDING("Understanding", "Being truly heard, seen, and empathized with."),
        ACCEPTANCE_AND_FORGIVENESS("Acceptance & Forgiveness",
                        "Feeling accepted without judgment and able to repair after hurt."),
        TRUST_AND_SAFETY("Trust & Safety", "Experiencing reliability, honesty, and emotional security."),
        SUPPORT("Support", "Receiving emotional encouragement or practical help."),
        BALANCE_AND_FAIRNESS("Balance & Fairness",
                        "Experiencing equity, shared effort, and mutual respect."),
        COMMUNICATION("Communication", "Expressing and discussing feelings openly and clearly."),
        PLAY_AND_JOY("Play & Joy", "Sharing lightness, humor, and fun."),
        GROWTH("Growth", "Developing, learning, and evolving individually and together."),
        GRATITUDE_AND_APPRECIATION("Gratitude & Appreciation",
                        "Feeling valued and acknowledged."),
        PRESENCE_AND_QUALITY_TIME("Presence & Quality Time",
                        "Sharing intentional, undistracted time together."),
        SPACE("Space", "Having autonomy and room for personal reflection.");

        private final String displayName;
        private final String description;
}
