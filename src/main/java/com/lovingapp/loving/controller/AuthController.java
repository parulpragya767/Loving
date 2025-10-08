package com.lovingapp.loving.controller;

import com.lovingapp.loving.dto.auth.LoginRequest;
import com.lovingapp.loving.dto.auth.LoginResponse;
import com.lovingapp.loving.service.auth.SupabaseAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("api/auth")
public class AuthController {

    private final SupabaseAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse resp = authService.login(request).block();
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(JwtAuthenticationToken token) {
        return ResponseEntity.ok(token.getTokenAttributes());
    }
}
