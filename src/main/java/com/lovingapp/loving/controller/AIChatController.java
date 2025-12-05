package com.lovingapp.loving.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.lovingapp.loving.model.dto.ChatDTOs.ChatSessionDTO;
import com.lovingapp.loving.model.dto.ChatDTOs.RecommendRitualPackResponse;
import com.lovingapp.loving.model.dto.ChatDTOs.SendMessageRequest;
import com.lovingapp.loving.model.dto.ChatDTOs.SendMessageResponse;
import com.lovingapp.loving.service.AIChatService;
import com.lovingapp.loving.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class AIChatController {

        private final AIChatService aiChatService;
        private final UserService userService;

        private UUID getAuthUserId(Jwt jwt) {
                String sub = jwt.getSubject();
                if (sub == null) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not present");
                }
                try {
                        UUID authUserId = UUID.fromString(sub);
                        return userService.getUserByAuthUserId(authUserId).getId();
                } catch (IllegalArgumentException e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user id in token");
                }
        }

        @GetMapping("/sessions/{sessionId}/messages")
        public ResponseEntity<ChatSessionDTO> getChatHistory(
                        @AuthenticationPrincipal Jwt jwt,
                        @PathVariable UUID sessionId) {
                return ResponseEntity.ok(aiChatService.getChatSessionWithMessages(sessionId));
        }

        @GetMapping("/sessions")
        public ResponseEntity<List<ChatSessionDTO>> listSessions(
                        @AuthenticationPrincipal Jwt jwt) {
                UUID userId = getAuthUserId(jwt);
                return ResponseEntity.ok(aiChatService.listSessions(userId));
        }

        @PostMapping("/sessions")
        public ResponseEntity<ChatSessionDTO> createSession(
                        @AuthenticationPrincipal Jwt jwt) {
                UUID userId = getAuthUserId(jwt);
                return ResponseEntity.ok(aiChatService.createSession(userId));
        }

        @PostMapping("/sessions/{sessionId}/messages")
        public ResponseEntity<SendMessageResponse> sendMessage(
                        @AuthenticationPrincipal Jwt jwt,
                        @PathVariable UUID sessionId,
                        @Valid @RequestBody SendMessageRequest request) {
                return ResponseEntity.ok(aiChatService.sendMessage(sessionId, request));
        }

        @PostMapping("/sessions/{sessionId}/recommend")
        public ResponseEntity<RecommendRitualPackResponse> recommendRitualPack(
                        @AuthenticationPrincipal Jwt jwt,
                        @PathVariable UUID sessionId) {
                UUID userId = getAuthUserId(jwt);
                return ResponseEntity.ok(aiChatService.recommendRitualPack(userId, sessionId));
        }

        @GetMapping("/sample-prompts")
        public ResponseEntity<List<String>> getSamplePrompts(
                        @AuthenticationPrincipal Jwt jwt) {
                UUID userId = getAuthUserId(jwt);
                return ResponseEntity.ok(aiChatService.getSamplePrompts(userId));
        }

        @DeleteMapping("/sessions/{sessionId}")
        public ResponseEntity<Void> deleteSession(
                        @AuthenticationPrincipal Jwt jwt,
                        @PathVariable UUID sessionId) {
                UUID userId = getAuthUserId(jwt);
                aiChatService.deleteSession(userId, sessionId);
                return ResponseEntity.noContent().build();
        }
}
