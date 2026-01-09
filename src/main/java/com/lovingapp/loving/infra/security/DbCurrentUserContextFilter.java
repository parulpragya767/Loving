package com.lovingapp.loving.infra.security;

import java.io.IOException;
import java.util.UUID;

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

@Component
@RequiredArgsConstructor
public class DbCurrentUserContextFilter extends OncePerRequestFilter {

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
            DbCurrentUserContext.setCurrentUserId(userId);
            filterChain.doFilter(request, response);
        } finally {
            DbCurrentUserContext.clear();
        }
    }
}
