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
        log.info("Fetch subscription request received for user={}", userId);

        SubscriptionDTO subscription = subscriptionService.getSubscription(userId);

        log.info("Subscription fetched successfully for user={}", userId);
        return ResponseEntity.ok(subscription);
    }
}
