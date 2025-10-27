package com.lovingapp.loving.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum EmojiFeedback {
    HEART, // ❤️ Loved it
    SMILE, // 🙂 Good
    NEUTRAL, // 😐 Okay
    SAD, // 🙁 Didn't like
    ANGRY, // 😠 Bad experience
    FIRE, // 🔥 Amazing
    THUMBS_UP, // 👍 Positive
    THUMBS_DOWN // 👎 Negative
}
