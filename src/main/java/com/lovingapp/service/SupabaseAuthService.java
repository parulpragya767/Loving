package com.lovingapp.service;

import java.util.UUID;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.lovingapp.config.database.SupabaseProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupabaseAuthService {

    private final SupabaseProperties supabaseProperties;
    private final RestTemplate restTemplate;

    /**
     * Deletes a user from Supabase Auth using the Admin API.
     * 
     * @param authUserId The Supabase auth user ID to delete
     * @throws RuntimeException if the deletion fails
     */
    public void deleteAuthUser(UUID authUserId) {
        String url = supabaseProperties.getUrl() + "/auth/v1/admin/users/" + authUserId;

        log.info("Attempting to delete Supabase auth user authUserId={}", authUserId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseProperties.getSecretKey());
        headers.set("apikey", supabaseProperties.getSecretKey());

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    requestEntity,
                    Void.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully deleted Supabase auth user authUserId={}", authUserId);
            } else {
                log.error("Failed to delete Supabase auth user authUserId={}, status={}",
                        authUserId, response.getStatusCode());
                throw new RuntimeException("Failed to delete Supabase auth user: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error deleting Supabase auth user authUserId={}", authUserId, e);
            throw new RuntimeException("Error deleting Supabase auth user: " + e.getMessage(), e);
        }
    }
}
