package com.lovingapp.loving.service.ai;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovingapp.loving.config.LlmClientProperties;
import com.lovingapp.loving.model.dto.ai.LlmResponse;
import com.lovingapp.loving.model.enums.EmotionalState;
import com.lovingapp.loving.model.enums.LoveType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * OpenAI implementation of the LlmClient interface.
 */
@Slf4j
@Component
public class OpenAiChatClient implements LlmClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final LlmClientProperties.OpenAiProperties openAiProps;
    private final WebClient webClient;

    public OpenAiChatClient(LlmClientProperties llmClientProperties, WebClient webClient) {
        this.openAiProps = llmClientProperties.getOpenai();
        this.webClient = webClient;
    }

    @Override
    public Mono<LlmResponse> chat(List<Message> messages) {
        OpenAiRequest request = OpenAiRequest.builder()
                .model(openAiProps.getModel())
                .maxTokens(openAiProps.getMaxTokens())
                .temperature(openAiProps.getTemperature())
                .messages(messages)
                .build();

        return webClient.post()
                .uri(openAiProps.getBaseUrl() + "/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiProps.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(this::parseLlmResponse)
                .onErrorResume(e -> {
                    log.error("Error calling OpenAI API", e);
                    return Mono.error(new RuntimeException("Failed to get response from OpenAI", e));
                });
    }

    @SuppressWarnings("unchecked")
    private Mono<LlmResponse> parseLlmResponse(String jsonResponse) {
        try {
            // Parse the response as a Map first to access fields directly
            Map<String, Object> responseMap = objectMapper.readValue(jsonResponse,
                    new TypeReference<Map<String, Object>>() {
                    });

            // Get the first choice's message content
            String content = "";
            if (responseMap.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    if (firstChoice.containsKey("message")) {
                        Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                        if (message != null && message.containsKey("content")) {
                            content = message.get("content").toString();
                        }
                    }
                }
            }

            // Parse the content as JSON to extract response and context
            try {
                // The content should be a JSON string with response and context
                Map<String, Object> llmResponse = objectMapper.readValue(content,
                        new TypeReference<Map<String, Object>>() {
                        });

                // Extract the response text
                String responseText = (String) llmResponse.get("response");

                // Extract context if available
                LlmResponse.Context context = null;
                if (llmResponse.containsKey("context")) {
                    Map<String, Object> contextMap = (Map<String, Object>) llmResponse.get("context");

                    if (contextMap != null) {
                        List<String> emotionalStates = (List<String>) contextMap.get("emotional_states");
                        List<String> loveTypes = (List<String>) contextMap.get("love_types");

                        context = LlmResponse.Context.builder()
                                .emotionalStates(emotionalStates != null ? emotionalStates.stream()
                                        .map(EmotionalState::valueOf)
                                        .collect(Collectors.toList()) : Collections.emptyList())
                                .loveTypes(loveTypes != null ? loveTypes.stream()
                                        .map(LoveType::valueOf)
                                        .collect(Collectors.toList()) : Collections.emptyList())
                                .needsFollowUp(Boolean.TRUE.equals(contextMap.get("needs_follow_up")))
                                .readyForRecommendation(Boolean.TRUE.equals(contextMap.get("ready_for_recommendation")))
                                .build();
                    }
                }

                // Build and return the LlmResponse
                return Mono.just(LlmResponse.builder()
                        .response(responseText)
                        .context(context != null ? context
                                : LlmResponse.Context.builder()
                                        .emotionalStates(Collections.emptyList())
                                        .loveTypes(Collections.emptyList())
                                        .needsFollowUp(false)
                                        .readyForRecommendation(false)
                                        .build())
                        .build());
            } catch (Exception e) {
                // If parsing as JSON fails, treat the entire content as the response
                log.warn("Failed to parse LLM response as JSON, using raw content", e);
                return Mono.just(LlmResponse.builder()
                        .response(content)
                        .context(LlmResponse.Context.builder()
                                .emotionalStates(Collections.emptyList())
                                .loveTypes(Collections.emptyList())
                                .needsFollowUp(false)
                                .readyForRecommendation(false)
                                .build())
                        .build());
            }
        } catch (Exception e) {
            log.error("Error parsing OpenAI API response", e);
            return Mono.error(new RuntimeException("Failed to parse OpenAI API response", e));
        }
    }

    // Request/Response DTOs for OpenAI API
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class OpenAiRequest {
        private String model;
        @JsonProperty("max_tokens")
        private Integer maxTokens;
        private Double temperature;
        private List<Message> messages;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class OpenAiResponse {
        private List<Choice> choices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class Choice {
        private int index;
        private OpenAiMessage message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class OpenAiMessage {
        private String role;
        private String content;
    }
}
