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

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/user-contexts")
@RequiredArgsConstructor
public class UserContextController {

    private final UserContextService userContextService;

    @PostMapping
    public ResponseEntity<UserContextDTO> createUserContext(
            @CurrentUser UUID userId,
            @Valid @RequestBody UserContextDTO userContextDTO) {
        userContextDTO.setId(null);
        userContextDTO.setUserId(userId);
        return ResponseEntity.ok(userContextService.createUserContext(userContextDTO));
    }

    @GetMapping
    public ResponseEntity<List<UserContextDTO>> getUserContexts(@CurrentUser UUID userId) {
        return ResponseEntity.ok(userContextService.getUserContexts(userId));
    }
}
