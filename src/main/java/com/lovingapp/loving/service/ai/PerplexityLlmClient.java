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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovingapp.loving.config.LlmClientProperties;
import com.lovingapp.loving.model.domain.LlmResponse;
import com.lovingapp.loving.model.enums.EmotionalState;
import com.lovingapp.loving.model.enums.LoveType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Perplexity AI implementation of the LlmClient interface.
 */
@Slf4j
@Component
public class PerplexityLlmClient implements LlmClient {

    private final LlmClientProperties.PerplexityProperties perplexityProps;
    private final WebClient webClient;

    public PerplexityLlmClient(LlmClientProperties llmClientProperties, WebClient webClient) {
        this.perplexityProps = llmClientProperties.getPerplexity();
        this.webClient = webClient;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<LlmResponse> chat(List<Message> messages) {
        // Convert Message objects to PerplexityMessage objects
        List<PerplexityMessage> perplexityMessages = messages.stream()
                .map(msg -> PerplexityMessage.builder()
                        .role(msg.role())
                        .content(msg.content())
                        .build())
                .toList();

        PerplexityRequest request = PerplexityRequest.builder()
                .model(perplexityProps.getModel())
                .maxTokens(perplexityProps.getMaxTokens())
                .temperature(perplexityProps.getTemperature())
                .topP(0.9) // Default value
                .topK(40) // Default value
                .stream(false)
                .presencePenalty(0.0)
                .frequencyPenalty(0.0)
                .messages(perplexityMessages)
                .build();

        // Log the request
        try {
            ObjectMapper mapper = new ObjectMapper();
            String requestBody = mapper.writeValueAsString(request);
            System.out.println("Sending request to Perplexity API:");
            System.out.println("URL: " + perplexityProps.getBaseUrl() + "/chat/completions");
            System.out.println("Headers: {Authorization: 'Bearer ' + [REDACTED], Content-Type: application/json}");
            System.out.println("Body: " + requestBody);
        } catch (Exception e) {
            System.err.println("Error logging request: " + e.getMessage());
        }

        return webClient.post()
                .uri(perplexityProps.getBaseUrl() + "/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + perplexityProps.getApiKey())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(this::parseLlmResponse)
                .onErrorResume(e -> {
                    log.error("Error calling Perplexity API", e);
                    return Mono
                            .error(new RuntimeException("Failed to get response from Perplexity: " + e.getMessage()));
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

            // Extract the response text and context from the content
            String responseText = content;
            LlmResponse.Context context = null;

            // Look for the context_json marker in the response
            int contextStart = content.indexOf("<context_json>");
            if (contextStart != -1) {
                // Extract the response text (everything before the context_json)
                responseText = content.substring(0, contextStart).trim();

                // Extract the JSON context
                int contextEnd = content.indexOf("</context_json>", contextStart);
                if (contextEnd != -1) {
                    String jsonContext = content.substring(contextStart + "<context_json>".length(), contextEnd).trim();
                    try {
                        Map<String, Object> contextMap = objectMapper.readValue(jsonContext,
                                new TypeReference<Map<String, Object>>() {
                                });

                        // Extract emotional states
                        List<String> emotionalStates = contextMap.containsKey("emotional_states")
                                ? (List<String>) contextMap.get("emotional_states")
                                : Collections.emptyList();

                        // Extract love types (if any)
                        List<String> loveTypes = contextMap.containsKey("love_types")
                                ? (List<String>) contextMap.get("love_types")
                                : Collections.emptyList();

                        // Build the context
                        context = LlmResponse.Context.builder()
                                .emotionalStates(emotionalStates.stream()
                                        .map(EmotionalState::valueOf)
                                        .collect(Collectors.toList()))
                                .loveTypes(loveTypes.stream()
                                        .map(LoveType::valueOf)
                                        .collect(Collectors.toList()))
                                .needsFollowUp(Boolean.TRUE.equals(contextMap.get("needs_follow_up")))
                                .readyForRecommendation(Boolean.TRUE.equals(contextMap.get("ready_for_recommendation")))
                                .build();

                    } catch (Exception e) {
                        log.warn("Failed to parse context JSON", e);
                    }
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
            log.error("Failed to parse response", e);
            return Mono.error(new RuntimeException("Failed to parse LLM response: " + e.getMessage()));
        }
    }

    // Request/Response DTOs for Perplexity API
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class PerplexityRequest {
        private String model;
        @JsonProperty("max_tokens")
        private Integer maxTokens;
        private Double temperature;
        @JsonProperty("top_p")
        private Double topP;
        @JsonProperty("top_k")
        private Integer topK;
        @JsonProperty("stream")
        private Boolean stream;
        @JsonProperty("presence_penalty")
        private Double presencePenalty;
        @JsonProperty("frequency_penalty")
        private Double frequencyPenalty;
        private List<PerplexityMessage> messages;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class PerplexityResponse {
        private String id;
        private String object;
        private long created;
        private String model;
        private List<Choice> choices;
        private Usage usage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class Choice {
        private int index;
        private PerplexityMessage message;
        @JsonProperty("finish_reason")
        private String finishReason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Usage {
        @JsonProperty("prompt_tokens")
        private int promptTokens;
        @JsonProperty("completion_tokens")
        private int completionTokens;
        @JsonProperty("total_tokens")
        private int totalTokens;
        @JsonProperty("search_context_size")
        private Integer searchContextSize; // Make it Integer to handle potential null
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class PerplexityMessage {
        @JsonProperty("role")
        private String role;
        @JsonProperty("content")
        private String content;
    }
}
