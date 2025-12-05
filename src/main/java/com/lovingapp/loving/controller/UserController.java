package com.lovingapp.loving.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.lovingapp.loving.model.dto.UserDTO;
import com.lovingapp.loving.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

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

    @PostMapping("/sync")
    public ResponseEntity<UserDTO> syncUser(@AuthenticationPrincipal Jwt jwt) {
        UUID authUserId = getAuthUserId(jwt);
        String email = jwt.getClaim("email");
        String displayName = jwt.getClaim("name");
        UserDTO dto = userService.syncUser(authUserId, email, displayName);
        return ResponseEntity.ok(dto);
    }

    @PutMapping()
    public ResponseEntity<UserDTO> updateUser(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UserDTO userDTO) {
        UUID authUserId = getAuthUserId(jwt);
        return ResponseEntity.ok(userService.updateUser(authUserId, userDTO));
    }
}
