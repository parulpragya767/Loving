package com.lovingapp.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents the current status or phase of a relationship.
 */
@Getter
@AllArgsConstructor
@Schema(enumAsRef = true)
public enum RelationshipStatus {
    NEW("A new connection still getting to know each other (0–3 months)."),
    ESTABLISHED("A growing relationship building trust and rhythm (3–12 months)."),
    COMMITTED("A long-term partnership with shared life and intentions."),
    ENGAGED("Preparing for marriage or a deeper long-term union."),
    MARRIED("Married or in a marriage-like life partnership."),
    REKINDLING("Working to rebuild closeness after distance or conflict."),
    LONG_DISTANCE("Maintaining connection while living apart."),
    CASUAL("Spending time together without long-term commitment."),
    EXPLORING("Getting to know each other and seeing what it may become."),
    OTHER("A unique or undefined relationship situation.");

    private final String description;
}
