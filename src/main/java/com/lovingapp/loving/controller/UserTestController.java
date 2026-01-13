package com.lovingapp.loving.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lovingapp.loving.auth.CurrentUser;
import com.lovingapp.loving.model.dto.UserContextDTO;
import com.lovingapp.loving.service.UserContextService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// @Profile("dev")
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/user-contexts")
@RequiredArgsConstructor
@Slf4j
public class UserTestController {

    private final UserContextService userContextService;

    @PostMapping
    public ResponseEntity<UserContextDTO> createUserContext(
            @CurrentUser UUID userId,
            @Valid @RequestBody UserContextDTO userContextDTO) {
        log.info("Create user context request received");
        log.debug("Create user context payload payload={}", userContextDTO);
        userContextDTO.setId(null);
        userContextDTO.setUserId(userId);
        UserContextDTO result = userContextService.createUserContext(userContextDTO);
        log.info("User context created successfully userContextId={}", result.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<UserContextDTO>> getUserContexts(@CurrentUser UUID userId) {
        log.info("Fetch user contexts request received");
        List<UserContextDTO> result = userContextService.getUserContexts(userId);
        log.info("User contexts fetched successfully count={}", result == null ? 0 : result.size());
        return ResponseEntity.ok(result);
    }
}
