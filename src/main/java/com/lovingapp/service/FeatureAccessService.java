package com.lovingapp.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.config.app.AppUsageLimitsProperties;
import com.lovingapp.config.app.AppUsageLimitsProperties.Limits;
import com.lovingapp.model.domain.FeatureAccessResult;
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

    public FeatureAccessResult checkAccess(UUID userId, FeatureType feature) {
        int remaining = getRemainingQuota(userId, feature);
        boolean allowed = remaining > 0;

        log.info("Checking access for user: {} and feature: {}, allowed: {}, remaining: {}", userId, feature, allowed,
                remaining);
        return new FeatureAccessResult(allowed, remaining);
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
