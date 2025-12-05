package com.lovingapp.loving.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.lovingapp.loving.model.dto.UserContextDTO;
import com.lovingapp.loving.service.UserContextService;
import com.lovingapp.loving.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/user-contexts")
@RequiredArgsConstructor
public class UserContextController {

    private final UserContextService userContextService;
    private final UserService userService;

    private UUID getAuthUserId(Jwt jwt) {
        String sub = jwt.getSubject();
        if (sub == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not present");
        }
        try {
            UUID authUserId = UUID.fromString(sub);
            return userService.getUserByAuthUserId(authUserId).getId();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user id in token");
        }
    }

    @PostMapping
    public ResponseEntity<UserContextDTO> createUserContext(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserContextDTO userContextDTO) {
        UUID userId = getAuthUserId(jwt);
        userContextDTO.setId(null);
        userContextDTO.setUserId(userId);
        return ResponseEntity.ok(userContextService.createUserContext(userContextDTO));
    }

    @GetMapping
    public ResponseEntity<List<UserContextDTO>> getUserContexts(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = getAuthUserId(jwt);
        return ResponseEntity.ok(userContextService.getUserContexts(userId));
    }
}
