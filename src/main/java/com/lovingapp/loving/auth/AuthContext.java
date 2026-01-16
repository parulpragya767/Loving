package com.lovingapp.loving.auth;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.lovingapp.loving.exception.ResourceNotFoundException;
import com.lovingapp.loving.model.dto.UserDTOs.UserDTO;
import com.lovingapp.loving.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthContext {

    private final UserService userService;

    private Jwt currentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new AccessDeniedException("Expected JWT authentication");
        }
        return jwtAuth.getToken();
    }

    public UUID getAuthUserId() {
        String sub = currentJwt().getSubject();
        if (sub == null) {
            throw new AccessDeniedException("Missing subject in token");
        }

        try {
            return UUID.fromString(sub);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid auth user id");
        }
    }

    public UserDTO getAppUser() {
        UUID authUserId = getAuthUserId();
        try {
            return userService.getUserByAuthUserId(authUserId);
        } catch (ResourceNotFoundException e) {
            throw new AccessDeniedException("User not found with authUserId");
        }
    }
}
