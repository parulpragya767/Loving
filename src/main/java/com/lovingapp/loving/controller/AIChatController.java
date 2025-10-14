package com.lovingapp.loving.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.lovingapp.loving.model.dto.ChatDTOs;
import com.lovingapp.loving.service.AIChatService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class AIChatController {

        private final AIChatService aiChatService;

        private UUID getAuthUserId(Jwt jwt) {
                String sub = jwt.getSubject();
                if (sub == null) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not present");
                }
                try {
                        return UUID.fromString(sub);
                } catch (IllegalArgumentException e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user id in token");
                }
        }

        /**
         * Start a new chat session or continue an existing one.
         * 
         * @param request The start session request
         * @return The response entity with the session details
         */
        @PostMapping("/sessions")
        public ResponseEntity<ChatDTOs.StartSessionResponse> startSession(
                        @AuthenticationPrincipal Jwt jwt,
                        @Valid @RequestBody ChatDTOs.StartSessionRequest request) {
                UUID userId = getAuthUserId(jwt);
                request.setUserId(userId);
                return ResponseEntity.ok(aiChatService.startSession(request));
        }

        /**
         * Send a message in an existing chat session.
         * 
         * @param sessionId The ID of the chat session
         * @param request   The message request
         * @return The response entity with the assistant's reply
         */
        @PostMapping("/sessions/{sessionId}/messages")
        public ResponseEntity<ChatDTOs.SendMessageResponse> sendMessage(
                        @AuthenticationPrincipal Jwt jwt,
                        @PathVariable UUID sessionId,
                        @Valid @RequestBody ChatDTOs.SendMessageRequest request) {
                UUID userId = getAuthUserId(jwt);
                return ResponseEntity.ok(aiChatService.sendMessage(sessionId, userId, request));
        }

        /**
         * Get the chat history for a session.
         * 
         * @param sessionId The ID of the chat session
         * @return The response entity with the chat history
         */
        @GetMapping("/sessions/{sessionId}/messages")
        public ResponseEntity<ChatDTOs.GetHistoryResponse> getChatHistory(
                        @AuthenticationPrincipal Jwt jwt,
                        @PathVariable UUID sessionId) {
                return ResponseEntity.ok(aiChatService.getChatHistory(sessionId));
        }
}
