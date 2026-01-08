package com.lovingapp.loving.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lovingapp.loving.auth.CurrentUser;
import com.lovingapp.loving.model.dto.ChatDTOs.ChatSessionDTO;
import com.lovingapp.loving.model.dto.ChatDTOs.RecommendRitualPackResponse;
import com.lovingapp.loving.model.dto.ChatDTOs.SendMessageRequest;
import com.lovingapp.loving.model.dto.ChatDTOs.SendMessageResponse;
import com.lovingapp.loving.service.AIChatService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class AIChatController {

        private final AIChatService aiChatService;

        @GetMapping("/sessions/{sessionId}/messages")
        public ResponseEntity<ChatSessionDTO> getChatHistory(
                        @CurrentUser UUID userId,
                        @PathVariable UUID sessionId) {
                return ResponseEntity.ok(aiChatService.getChatSessionWithMessages(userId, sessionId));
        }

        @GetMapping("/sessions")
        public ResponseEntity<List<ChatSessionDTO>> listSessions(@CurrentUser UUID userId) {
                return ResponseEntity.ok(aiChatService.listSessions(userId));
        }

        @PostMapping("/sessions")
        public ResponseEntity<ChatSessionDTO> createSession(@CurrentUser UUID userId) {
                return ResponseEntity.ok(aiChatService.createSession(userId));
        }

        @PostMapping("/sessions/{sessionId}/messages")
        public ResponseEntity<SendMessageResponse> sendMessage(
                        @CurrentUser UUID userId,
                        @PathVariable UUID sessionId,
                        @Valid @RequestBody SendMessageRequest request) {
                return ResponseEntity.ok(aiChatService.sendMessage(userId, sessionId, request));
        }

        @PostMapping("/sessions/{sessionId}/recommend")
        public ResponseEntity<RecommendRitualPackResponse> recommendRitualPack(
                        @CurrentUser UUID userId,
                        @PathVariable UUID sessionId) {
                return ResponseEntity.ok(aiChatService.recommendRitualPack(userId, sessionId));
        }

        @GetMapping("/sample-prompts")
        public ResponseEntity<List<String>> getSamplePrompts(
                        @CurrentUser UUID userId) {
                return ResponseEntity.ok(aiChatService.getSamplePrompts(userId));
        }

        @DeleteMapping("/sessions/{sessionId}")
        public ResponseEntity<Void> deleteSession(
                        @CurrentUser UUID userId,
                        @PathVariable UUID sessionId) {
                aiChatService.deleteSession(userId, sessionId);
                return ResponseEntity.noContent().build();
        }
}
