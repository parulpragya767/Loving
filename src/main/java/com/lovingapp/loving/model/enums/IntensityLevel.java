package com.lovingapp.loving.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents the desired intensity level for a ritual.
 */
@Schema(enumAsRef = true)
public enum IntensityLevel {
    LOW, // Gentle, relaxed activities
    MODERATE, // Balanced energy and engagement
    HIGH, // High energy and intensity
    INTENSE // Very high energy and intensity
}
