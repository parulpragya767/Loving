package com.lovingapp.loving.controller;

import java.util.UUID;

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
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final UserService userService;
    private final AuthContext authContext;

    @PostMapping("/sync")
    public ResponseEntity<UserDTO> syncUser(@AuthenticationPrincipal Jwt jwt) {
        UUID authUserId = authContext.getAuthUserId();
        String email = jwt.getClaim("email");

        log.info("User sync request received authUserId={}", authUserId);

        UserDTO dto = userService.syncUser(authUserId, email);
        MDC.put("userId", dto.getId().toString());

        log.info("User synced successfully authUserId={}", authUserId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping()
    public ResponseEntity<Void> updateUser(
            @CurrentUser UUID userId,
            @RequestBody UserUpdateRequest request) {
        log.info("User update request received");

        userService.updateUser(userId, request);

        log.info("User updated successfully");
        return ResponseEntity.ok().build();
    }
}
