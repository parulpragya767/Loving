package com.lovingapp.loving.config;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.lovingapp.loving.exception.ResourceNotFoundException;
import com.lovingapp.loving.service.UserService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthContext {

    private final UserService userService;

    public UUID getAuthUserId(Jwt jwt) {
        String sub = jwt.getSubject();
        if (sub == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing subject in token");
        }

        try {
            return UUID.fromString(sub);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid auth user id");
        }
    }

    public UUID getAppUserId(Jwt jwt) {
        UUID authUserId = getAuthUserId(jwt);
        try {
            return userService.getUserByAuthUserId(authUserId).getId();
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found with authUserId");
        }
    }
}
