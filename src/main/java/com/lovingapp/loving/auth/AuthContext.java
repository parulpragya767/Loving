package com.lovingapp.loving.auth;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

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
            log.info("Request is not authenticated with JWT");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Expected JWT authentication");
        }
        return jwtAuth.getToken();
    }

    public UUID getAuthUserId() {
        String sub = currentJwt().getSubject();
        if (sub == null) {
            log.info("JWT subject is missing");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing subject in token");
        }

        try {
            return UUID.fromString(sub);
        } catch (IllegalArgumentException e) {
            log.info("JWT subject is not a valid UUID");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid auth user id");
        }
    }

    public UserDTO getAppUser() {
        UUID authUserId = getAuthUserId();
        try {
            return userService.getUserByAuthUserId(authUserId);
        } catch (ResourceNotFoundException e) {
            log.info("Authenticated user is not present in application database");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found with authUserId");
        }
    }
}
