package com.lovingapp.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lovingapp.auth.CurrentUser;
import com.lovingapp.model.dto.UserContextDTOs.UserContextCreateRequest;
import com.lovingapp.model.dto.UserContextDTOs.UserContextDTO;
import com.lovingapp.model.dto.UserUsageCounterDTOs.UserUsageCounterDTO;
import com.lovingapp.service.UsageService;
import com.lovingapp.service.UserContextService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Profile("dev")
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/internal/dev-tools")
@RequiredArgsConstructor
@Slf4j
public class DevToolsController {

    private final UserContextService userContextService;
    private final UsageService usageService;

    @PostMapping("/user-contexts")
    public ResponseEntity<UserContextDTO> createUserContext(
            @CurrentUser UUID userId,
            @RequestBody UserContextCreateRequest request) {
        log.info("Create user context request received");

        UserContextDTO result = userContextService.create(userId, request);

        log.info("User context created successfully userContextId={}", result.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/user-contexts")
    public ResponseEntity<List<UserContextDTO>> getUserContexts(@CurrentUser UUID userId) {
        log.info("Fetch user contexts request received");

        List<UserContextDTO> result = userContextService.findAll(userId);

        log.info("User contexts fetched successfully count={}", result == null ? 0 : result.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/user-contexts/session/{id}")
    public ResponseEntity<List<UserContextDTO>> getUserContextsForConversation(
            @CurrentUser UUID userId,
            @PathVariable UUID id) {
        log.info("Fetch user contexts for conversation request received ConversationId={}", id);

        List<UserContextDTO> result = userContextService.findByConversationId(userId, id);

        log.info("User contexts fetched successfully count={}", result == null ? 0 : result.size());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/usage/increment/ai-message")
    public ResponseEntity<Void> incrementAiMessageUsage(@CurrentUser UUID userId) {
        log.info("Increment AI message usage request received");

        usageService.incrementAiMessageUsage(userId);

        log.info("AI message usage incremented successfully");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/usage/increment/recommendation")
    public ResponseEntity<Void> incrementRecommendationUsage(@CurrentUser UUID userId) {
        log.info("Increment recommendation usage request received");

        usageService.incrementRecommendationUsage(userId);

        log.info("Recommendation usage incremented successfully");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/usage/daily")
    public ResponseEntity<UserUsageCounterDTO> getDailyUsage(@CurrentUser UUID userId) {
        log.info("Get daily usage request received");

        UserUsageCounterDTO result = usageService.getDailyUsage(userId);

        log.info("Daily usage fetched successfully usageCounterId: {}", result.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/usage/weekly")
    public ResponseEntity<UserUsageCounterDTO> getWeeklyUsage(@CurrentUser UUID userId) {
        log.info("Get weekly usage request received");

        UserUsageCounterDTO result = usageService.getWeeklyUsage(userId);

        log.info("Weekly usage fetched successfully usageCounterId: {}", result.getId());
        return ResponseEntity.ok(result);
    }
}
