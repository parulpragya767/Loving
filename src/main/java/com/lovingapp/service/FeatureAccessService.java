package com.lovingapp.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.config.app.AppUsageLimitsProperties;
import com.lovingapp.config.app.AppUsageLimitsProperties.Limits;
import com.lovingapp.exception.UsageLimitExceededException;
import com.lovingapp.model.enums.FeatureType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FeatureAccessService {

    private final SubscriptionService subscriptionService;
    private final UsageService usageService;
    private final AppUsageLimitsProperties usageLimitsProperties;

    public void assertAccess(UUID userId, FeatureType feature) {
        int remaining = getRemainingQuota(userId, feature);

        if (remaining <= 0) {
            throw new UsageLimitExceededException(feature);
        }

        log.info("Access granted for feature: {}, remaining: {}", feature, remaining);
    }

    public int getRemainingQuota(UUID userId, FeatureType feature) {
        Limits limits = resolveLimits(userId);

        return switch (feature) {
            case AI_CHAT -> {
                int usage = usageService.getDailyUsage(userId).getAiMessagesCount();
                yield Math.max(0, limits.getDailyAiMessages() - usage);
            }
            case AI_RECOMMENDATION -> {
                int usage = usageService.getWeeklyUsage(userId).getRecommendationsCount();
                yield Math.max(0, limits.getWeeklyRecommendations() - usage);
            }
        };
    }

    private Limits resolveLimits(UUID userId) {
        if (subscriptionService.hasAccessToPremiumFeatures(userId)) {
            return usageLimitsProperties.getPremium();
        }
        return usageLimitsProperties.getFree();
    }
}
