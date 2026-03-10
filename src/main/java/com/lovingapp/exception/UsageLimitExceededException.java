package com.lovingapp.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UsageLimitExceededException extends RuntimeException {

    private final String limitMessage;
    private final Type type;

    public enum Type {
        AI_DAILY_LIMIT_REACHED,
        WEEKLY_RECOMMENDATION_LIMIT_REACHED,
        PREMIUM_REQUIRED
    }

    public UsageLimitExceededException(Type type) {
        super(getMessageForType(type));
        this.type = type;
        this.limitMessage = getMessageForType(type);
    }

    public UsageLimitExceededException(Type type, String customMessage) {
        super(customMessage);
        this.type = type;
        this.limitMessage = customMessage;
    }

    public Type getType() {
        return type;
    }

    public String getLimitMessage() {
        return limitMessage;
    }

    private static String getMessageForType(Type type) {
        return switch (type) {
            case AI_DAILY_LIMIT_REACHED -> "Daily AI usage limit has been reached. Please try again tomorrow.";
            case WEEKLY_RECOMMENDATION_LIMIT_REACHED ->
                "Weekly recommendation limit has been reached. Please try again next week.";
            case PREMIUM_REQUIRED -> "This feature requires a premium subscription.";
        };
    }
}
