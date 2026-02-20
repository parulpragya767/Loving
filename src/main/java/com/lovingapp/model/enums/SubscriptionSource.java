package com.lovingapp.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum SubscriptionSource {
    APPLE,
    GOOGLE_PLAY,
    STRIPE,
    INTERNAL,
    NONE
}