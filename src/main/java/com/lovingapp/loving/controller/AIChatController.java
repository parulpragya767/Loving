package com.lovingapp.loving.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lovingapp.loving.model.dto.ChatDTOs;
import com.lovingapp.loving.model.dto.ChatDTOs.GetHistoryResponse;
import com.lovingapp.loving.service.AIChatService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/ai-chat")
@RequiredArgsConstructor
public class AIChatController {

        private final AIChatService aiChatService;

        /**
         * Start a new chat session or continue an existing one.
         * 
         * @param request The start session request
         * @return A Mono emitting the response entity with the session details
         */
        @PostMapping("/sessions")
        public Mono<ResponseEntity<ChatDTOs.StartSessionResponse>> startSession(
                        @Valid @RequestBody ChatDTOs.StartSessionRequest request) {
                return aiChatService.startSession(request)
                                .map(ResponseEntity::ok);
        }

        /**
         * Send a message in an existing chat session.
         * 
         * @param sessionId The ID of the chat session
         * @param request   The message request
         * @return A Mono emitting the response entity with the assistant's reply
         */
        @PostMapping("/sessions/{sessionId}/messages")
        public Mono<ResponseEntity<ChatDTOs.SendMessageResponse>> sendMessage(
                        @PathVariable UUID sessionId,
                        @Valid @RequestBody ChatDTOs.SendMessageRequest request) {
                return aiChatService.sendMessage(sessionId, request)
                                .map(ResponseEntity::ok);
        }

        /**
         * Get the chat history for a session.
         * 
         * @param sessionId The ID of the chat session
         * @return A Mono emitting the response entity with the chat history
         */
        @GetMapping("/sessions/{sessionId}/history")
        public Mono<ResponseEntity<GetHistoryResponse>> getChatHistory(
                        @PathVariable UUID sessionId) {
                return aiChatService.getChatHistory(sessionId)
                                .map(ResponseEntity::ok);
        }
}
