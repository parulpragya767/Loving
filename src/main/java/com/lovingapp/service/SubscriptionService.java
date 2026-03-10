package com.lovingapp.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.exception.ResourceNotFoundException;
import com.lovingapp.model.dto.SubscriptionDTO;
import com.lovingapp.model.entity.User;
import com.lovingapp.model.enums.SubscriptionStatus;
import com.lovingapp.model.enums.SubscriptionTier;
import com.lovingapp.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SubscriptionService {

    private final UserRepository userRepository;
    private final FeatureFlagService featureFlagService;

    public SubscriptionDTO getSubscription(UUID userId) {
        User user = findUserById(userId);
        boolean hasAccess = hasAccessToPremiumFeatures(userId);

        return SubscriptionDTO.builder()
                .tier(user.getSubscriptionTier())
                .status(user.getSubscriptionStatus())
                .expiresAt(user.getSubscriptionExpiresAt())
                .isBetaUser(user.getIsBetaUser())
                .hasPremiumAccess(hasAccess)
                .build();
    }

    public boolean hasActiveSubscription(UUID userId) {
        User user = findUserById(userId);
        return hasActiveSubscription(user);
    }

    public boolean hasAccessToPremiumFeatures(UUID userId) {
        if (!featureFlagService.isPremiumEnabled()) {
            return true;
        }

        User user = findUserById(userId);
        return hasActiveSubscription(user) || user.getIsBetaUser();
    }

    private boolean hasActiveSubscription(User user) {
        if (user.getSubscriptionTier() == SubscriptionTier.PREMIUM &&
                (user.getSubscriptionStatus() == SubscriptionStatus.ACTIVE ||
                        user.getSubscriptionStatus() == SubscriptionStatus.TRIALING ||
                        user.getSubscriptionStatus() == SubscriptionStatus.GRACE_PERIOD)) {

            if (user.getSubscriptionExpiresAt() == null) {
                return true;
            }

            return user.getSubscriptionExpiresAt().isAfter(OffsetDateTime.now());
        }
        return false;
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
}
