package com.lovingapp.loving.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lovingapp.loving.auth.AuthContext;
import com.lovingapp.loving.auth.CurrentUser;
import com.lovingapp.loving.model.dto.UserDTOs.UserDTO;
import com.lovingapp.loving.model.dto.UserDTOs.UserUpdateRequest;
import com.lovingapp.loving.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthContext authContext;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/sync")
    public ResponseEntity<UserDTO> syncUser(@AuthenticationPrincipal Jwt jwt) {
        UUID authUserId = authContext.getAuthUserId();
        String email = jwt.getClaim("email");

        log.info("User sync call received");

        UserDTO dto = userService.syncUser(authUserId, email);
        MDC.put("user_id", dto.getId().toString());

        log.info("User synced successfully");
        return ResponseEntity.ok(dto);
    }

    @PutMapping()
    public ResponseEntity<UserDTO> updateUser(
            @CurrentUser UUID userId,
            @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }
}
