package com.lovingapp.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum SubscriptionStatus {
    INACTIVE, // No subscription
    ACTIVE, // Paid and valid
    TRIALING, // In trial period
    GRACE_PERIOD, // Payment failed but still allowed
    EXPIRED, // Subscription ended
    CANCELLED // User cancelled but still valid until expiry
}
