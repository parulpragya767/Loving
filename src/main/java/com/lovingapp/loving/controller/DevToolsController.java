package com.lovingapp.loving.controller;

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

import com.lovingapp.loving.auth.CurrentUser;
import com.lovingapp.loving.model.dto.UserContextDTOs.UserContextCreateRequest;
import com.lovingapp.loving.model.dto.UserContextDTOs.UserContextDTO;
import com.lovingapp.loving.service.UserContextService;

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
}
