package com.lovingapp.loving.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lovingapp.loving.config.AuthContext;
import com.lovingapp.loving.config.CurrentUser;
import com.lovingapp.loving.model.dto.UserDTO;
import com.lovingapp.loving.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthContext authContext;

    @PostMapping("/sync")
    public ResponseEntity<UserDTO> syncUser(@AuthenticationPrincipal Jwt jwt) {
        UUID authUserId = authContext.getAuthUserId();
        String email = jwt.getClaim("email");
        String displayName = jwt.getClaim("name");
        UserDTO dto = userService.syncUser(authUserId, email, displayName);
        return ResponseEntity.ok(dto);
    }

    @PutMapping()
    public ResponseEntity<UserDTO> updateUser(
            @CurrentUser UserDTO user,
            @RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.updateUser(user.getId(), dto));
    }
}
