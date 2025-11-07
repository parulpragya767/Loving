package com.lovingapp.loving.model.enums;

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
    NEW("A new connection, still discovering each other (0–3 months)."),
    ESTABLISHED("A growing relationship building trust and rhythm (3–12 months)."),
    COMMITTED("A long-term partnership grounded in shared life and intentions (1+ years)."),
    ENGAGED("Committed to a shared future and preparing for marriage or deeper union."),
    MARRIED("In a marriage or marriage-like commitment, sharing life closely."),
    REKINDLING("Working on repairing or renewing emotional closeness after distance or conflict."),
    LONG_DISTANCE("Maintaining love and connection while living apart."),
    CASUAL("Dating or spending time together without long-term commitment."),
    EXPLORING("Getting to know each other and seeing what the connection might become."),
    OTHER("A unique or undefined relationship status not covered above.");

    private final String description;
}
