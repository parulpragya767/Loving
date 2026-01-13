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
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class AIChatController {

    private final AIChatService aiChatService;

    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ChatSessionDTO> getChatSessionWithHistory(
            @CurrentUser UUID userId,
            @PathVariable UUID sessionId) {
        log.info("Fetch chat session with messages request received sessionId={}", sessionId);

        ChatSessionDTO result = aiChatService.getChatSessionWithMessages(userId, sessionId);

        log.info("Chat session with messages fetched successfully sessionId={}, messagesCount={}", sessionId,
                result.getMessages().size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSessionDTO>> listSessions(@CurrentUser UUID userId) {
        log.info("List chat sessions request received");

        List<ChatSessionDTO> result = aiChatService.listSessions(userId);

        log.info("Chat sessions fetched successfully count={}", result == null ? 0 : result.size());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/sessions")
    public ResponseEntity<ChatSessionDTO> createSession(@CurrentUser UUID userId) {
        log.info("Create chat session request received");

        ChatSessionDTO result = aiChatService.createSession(userId);

        log.info("Chat session created successfully sessionId={}", result.getId());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<SendMessageResponse> sendMessage(
            @CurrentUser UUID userId,
            @PathVariable UUID sessionId,
            @Valid @RequestBody SendMessageRequest request) {
        log.info("Send message request received sessionId={}", sessionId);

        SendMessageResponse result = aiChatService.sendMessage(userId, sessionId, request);

        log.info("Message sent successfully sessionId={}", sessionId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/sessions/{sessionId}/recommend")
    public ResponseEntity<RecommendRitualPackResponse> recommendRitualPack(
            @CurrentUser UUID userId,
            @PathVariable UUID sessionId) {
        log.info("Ritual pack recommendation request received sessionId={}", sessionId);
        RecommendRitualPackResponse result = aiChatService.recommendRitualPack(userId, sessionId);
        log.info("Ritual pack recommended successfully sessionId={}", sessionId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/sample-prompts")
    public ResponseEntity<List<String>> getSamplePrompts() {
        log.info("Sample prompts request received");

        List<String> result = aiChatService.getSamplePrompts();

        log.info("Sample prompts fetched successfully count={}", result == null ? 0 : result.size());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(
            @CurrentUser UUID userId,
            @PathVariable UUID sessionId) {
        log.info("Delete chat session request received sessionId={}", sessionId);

        aiChatService.deleteSession(userId, sessionId);

        log.info("Chat session deleted successfully sessionId={}", sessionId);
        return ResponseEntity.noContent().build();
    }
}
