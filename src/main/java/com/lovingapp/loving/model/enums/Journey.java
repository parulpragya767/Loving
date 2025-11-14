package com.lovingapp.loving.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true)
public enum Journey {

        FEELING_DISTANT(
                        "Feeling Distant",
                        "When emotional connection feels thin or you’re drifting apart inside."),

        LOVE_FEELS_FLAT(
                        "Love Feels Flat",
                        "When passion or excitement feels lost — when life together feels routine or uninspired, and the spark has dimmed."),

        LOST_TOUCH(
                        "Lost Touch",
                        "When physical intimacy or affection has faded, and you want to feel warmth and closeness again."),

        CARRYING_TOO_MUCH(
                        "Carrying Too Much",
                        "When the daily grind, imbalance of responsibility, or mental overload leaves you tired and disconnected."),

        WEATHERING_A_STORM(
                        "Weathering a Storm",
                        "Facing external stress or life transitions together and needing steadiness and support."),

        BRIDGING_THE_DIVIDE(
                        "Bridging the Divide",
                        "Learning to repair and reconnect after conflict, misunderstanding, or hurt."),

        LEARNING_TO_HEAR_EACH_OTHER(
                        "Learning to Hear Each Other",
                        "Strengthening communication, empathy, and the ability to really listen and feel heard."),

        MAKING_SPACE_FOR_US(
                        "Making Space for Us",
                        "Prioritising quality time and togetherness amid busy lives or distractions."),

        KEEP_THE_LOVE_ALIVE(
                        "Keep the Love Alive",
                        "Nurturing affection and connection through small, daily gestures and presence."),

        GROW_AND_EVOLVE_TOGETHER(
                        "Grow and Evolve Together",
                        "Encouraging mutual growth, learning, and transformation as partners and individuals."),

        RETURN_TO_SELF(
                        "Return to Self",
                        "Reconnecting with your own inner world, needs, and self-worth — so love can flow from wholeness."),

        CELEBRATE_US(
                        "Celebrate Us",
                        "Honouring love, milestones, or shared gratitude — remembering what you’ve built together.");

        private final String displayName;
        private final String description;
}
