package com.lovingapp.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true)
public enum Journey {

        FEELING_DISTANT(
                        "Feeling Distant",
                        "When emotional connection feels thin and you sense yourselves drifting apart."),

        LOVE_FEELS_FLAT(
                        "Love Feels Flat",
                        "When passion or excitement has faded and life together feels routine or uninspired."),

        LOST_TOUCH(
                        "Lost Touch",
                        "When physical intimacy or affection has faded, and you want to feel warmth and closeness again."),

        CARRYING_TOO_MUCH(
                        "Carrying Too Much",
                        "When responsibilities, imbalance, or mental load leave you tired and disconnected."),

        WEATHERING_A_STORM(
                        "Weathering a Storm",
                        "When external stress or life transitions test the relationship and you need steadiness and support."),

        BRIDGING_THE_DIVIDE(
                        "Bridging the Divide",
                        "When conflict, misunderstanding, or hurt has created distance and repair is needed."),

        LEARNING_TO_HEAR_EACH_OTHER(
                        "Learning to Hear Each Other",
                        "When communication feels difficult and you want to listen, understand, and feel heard more deeply."),

        MAKING_SPACE_FOR_US(
                        "Making Space for Us",
                        "When busy lives or distractions leave little time for each other and you want to reconnect."),

        KEEP_THE_LOVE_ALIVE(
                        "Keep the Love Alive",
                        "When you want to keep love vibrant through small, everyday gestures of affection and presence."),

        GROW_AND_EVOLVE_TOGETHER(
                        "Grow and Evolve Together",
                        "When you want to support each other's growth and evolve together as partners and individuals."),

        RETURN_TO_SELF(
                        "Return to Self",
                        "When you need to reconnect with your own inner world, needs, and sense of self."),

        CELEBRATE_US(
                        "Celebrate Us",
                        "When you want to honour your relationship, celebrate milestones, or express shared gratitude.");

        private final String displayName;
        private final String description;
}
