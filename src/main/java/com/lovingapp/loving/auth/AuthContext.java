package com.lovingapp.loving.auth;

import java.util.Optional;
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
            throw new AccessDeniedException("JWT authentication required");
        }
        return jwtAuth.getToken();
    }

    public UUID getAuthUserId() {
        String subject = currentJwt().getSubject();
        if (subject == null) {
            throw new AccessDeniedException("JWT subject is missing");
        }

        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            throw new AccessDeniedException("JWT subject is not a valid UUID");
        }
    }

    public UserDTO getAppUser() {
        UUID authUserId = getAuthUserId();
        return userService.getUserByAuthUserId(authUserId);
    }

    public Optional<UserDTO> resolveAppUser() {
        try {
            return Optional.of(getAppUser());
        } catch (ResourceNotFoundException e) {
            return Optional.empty();
        }
    }
}
