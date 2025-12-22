package com.lovingapp.loving.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum RitualFeedback {
    WARM, // ❤️ connection, care
    JOYFUL, // 😊 light positive
    CALM, // 😌 grounded
    NEUTRAL, // 😐 no strong feeling
    SAD, // 😢 soft negative
    FRUSTRATED, // 😠 activated negative
    ENERGIZED // 🔥 intense / activated positive
}
