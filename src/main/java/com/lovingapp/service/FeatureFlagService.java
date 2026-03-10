package com.lovingapp.service;

import org.springframework.stereotype.Service;

import com.lovingapp.config.app.FeatureFlagsProperties;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeatureFlagService {

    private final FeatureFlagsProperties featureFlags;

    public boolean isPaymentsEnabled() {
        return featureFlags.isPaymentsEnabled();
    }

    public boolean isPremiumEnabled() {
        return featureFlags.isPremiumEnabled();
    }
}
