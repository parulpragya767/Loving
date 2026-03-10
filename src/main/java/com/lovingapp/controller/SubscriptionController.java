package com.lovingapp.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lovingapp.auth.CurrentUser;
import com.lovingapp.model.dto.SubscriptionDTO;
import com.lovingapp.service.SubscriptionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/v1/subscription")
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<SubscriptionDTO> getSubscription(@CurrentUser UUID userId) {
        log.info("Fetch subscription request received");

        SubscriptionDTO subscription = subscriptionService.getSubscription(userId);

        log.info("Subscription fetched successfully");
        return ResponseEntity.ok(subscription);
    }

    @GetMapping("/active")
    public ResponseEntity<Boolean> hasActiveSubscription(@CurrentUser UUID userId) {
        log.info("Check if user has active subscription request received");

        boolean hasActive = subscriptionService.hasActiveSubscription(userId);

        log.info("User has active subscription: {}", hasActive);
        return ResponseEntity.ok(hasActive);
    }

    @GetMapping("/premium")
    public ResponseEntity<Boolean> hasAccessToPremiumFeatures(@CurrentUser UUID userId) {
        log.info("Check if user has access to premium features request received");

        boolean hasAccess = subscriptionService.hasAccessToPremiumFeatures(userId);

        log.info("User has access to premium features: {}", hasAccess);
        return ResponseEntity.ok(hasAccess);
    }
}
