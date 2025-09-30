package com.lovingapp.loving.model.enums;

/**
 * Represents the current status or phase of a relationship.
 */
public enum RelationshipStatus {
    NEW,            // New relationship (0-3 months)
    ESTABLISHED,    // Established but still growing (3-12 months)
    COMMITTED,      // Committed long-term (1+ years)
    ENGAGED,        // Engaged to be married
    MARRIED,        // Married or in a marriage-like commitment
    REKINDLING,     // Working on reconnecting
    LONG_DISTANCE,  // In a long-distance relationship
    CASUAL,         // Casual dating
    EXPLORING,      // Exploring the relationship
    OTHER           // Other status not listed
}
