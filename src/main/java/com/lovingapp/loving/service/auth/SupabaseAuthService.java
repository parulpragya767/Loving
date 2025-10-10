package com.lovingapp.loving.service.auth;

import com.lovingapp.loving.config.SupabaseProperties;
import com.lovingapp.loving.model.dto.auth.LoginRequest;
import com.lovingapp.loving.model.dto.auth.LoginResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SupabaseAuthService {

    private final WebClient webClient;
    private final SupabaseProperties supabaseProperties;

    public Mono<LoginResponse> login(LoginRequest request) {
        String url = supabaseProperties.getUrl().replaceAll("/$", "") + "/auth/v1/token?grant_type=password";
        return webClient
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("apikey", supabaseProperties.getAnonKey())
                .bodyValue(Map.of(
                        "email", request.getEmail(),
                        "password", request.getPassword()))
                .retrieve()
                .bodyToMono(LoginResponse.class);
    }
}
