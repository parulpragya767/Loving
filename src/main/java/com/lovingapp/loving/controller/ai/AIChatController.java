package com.lovingapp.loving.controller.ai;

import com.lovingapp.loving.dto.ai.ChatDTOs;
import com.lovingapp.loving.service.ai.AIChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/ai-chat")
@RequiredArgsConstructor
public class AIChatController {

    private final AIChatService aiChatService;

    @PostMapping("/sessions")
    public ResponseEntity<ChatDTOs.StartSessionResponse> startSession(
            @Valid @RequestBody ChatDTOs.StartSessionRequest request
    ) {
        return ResponseEntity.ok(aiChatService.startSession(request));
    }

    @PostMapping("/sessions/{sessionId}/messages")
    public Mono<ResponseEntity<ChatDTOs.SendMessageResponse>> sendMessage(
            @PathVariable UUID sessionId,
            @Valid @RequestBody ChatDTOs.SendMessageRequest request
    ) {
        return aiChatService.sendMessage(sessionId, request)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/sessions/{sessionId}/history")
    public ResponseEntity<ChatDTOs.HistoryResponse> getHistory(
            @PathVariable UUID sessionId
    ) {
        return ResponseEntity.ok(aiChatService.getHistory(sessionId));
    }
}
