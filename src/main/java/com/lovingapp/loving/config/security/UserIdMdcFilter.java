package com.lovingapp.loving.config.security;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.lovingapp.loving.auth.AuthContext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserIdMdcFilter extends OncePerRequestFilter {

    private final AuthContext authContext;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            UUID userId = authContext.getAppUser().getId();
            if (userId != null && !userId.toString().isBlank()) {
                MDC.put("userId", userId.toString());
            }
            filterChain.doFilter(request, response);
            // } catch (Exception e) {
            // log.error("Failed to populate user context into MDC", e);
            // throw e;
        } finally {
            MDC.remove("userId");
        }
    }
}
