package com.lovingapp.loving.service.ai;

import com.lovingapp.loving.dto.ai.LlmResponse;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Interface for LLM provider clients.
 * Implement this for different providers (OpenAI, Perplexity, etc.)
 */
public interface LlmClient {
    /**
     * Send a chat completion request to the LLM provider.
     * @param messages List of messages in the conversation
     * @return Mono that emits the parsed LLM response
     */
    Mono<LlmResponse> chat(List<Message> messages);

    /**
     * Message role and content for the LLM.
     */
    record Message(String role, String content) {
        public static Message of(String role, String content) {
            return new Message(role, content);
        }
    }
}
