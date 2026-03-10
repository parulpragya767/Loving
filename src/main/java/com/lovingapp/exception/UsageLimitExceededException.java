package com.lovingapp.exception;

import com.lovingapp.model.enums.FeatureType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UsageLimitExceededException extends RuntimeException {

    private final String limitMessage;
    private final FeatureType featureType;

    public UsageLimitExceededException(FeatureType featureType) {
        super(getMessageForType(featureType));
        this.featureType = featureType;
        this.limitMessage = getMessageForType(featureType);
    }

    public UsageLimitExceededException(FeatureType featureType, String customMessage) {
        super(customMessage);
        this.featureType = featureType;
        this.limitMessage = customMessage;
    }

    public FeatureType getFeatureType() {
        return featureType;
    }

    public String getLimitMessage() {
        return limitMessage;
    }

    private static String getMessageForType(FeatureType featureType) {
        return switch (featureType) {
            case AI_CHAT -> "Daily AI usage limit has been reached. Please try again tomorrow.";
            case RITUAL_PACK_RECOMMENDATION ->
                "Weekly recommendation limit has been reached. Please try again next week.";
        };
    }
}
