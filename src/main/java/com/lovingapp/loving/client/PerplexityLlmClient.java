package com.lovingapp.loving.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovingapp.loving.config.LlmClientProperties;
import com.lovingapp.loving.helpers.ai.LLMResponseParser;
import com.lovingapp.loving.model.domain.ai.LLMRequest;
import com.lovingapp.loving.model.domain.ai.LLMResponse;
import com.lovingapp.loving.model.enums.ChatMessageRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Perplexity AI implementation of the LlmClient interface.
 */
@Slf4j
public class PerplexityLlmClient implements LlmClient {

    private final LlmClientProperties.PerplexityProperties perplexityProps;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PerplexityLlmClient(LlmClientProperties llmClientProperties,
            WebClient webClient) {
        this.perplexityProps = llmClientProperties.getPerplexity();
        this.webClient = webClient;
    }

    @Override
    public <T> LLMResponse<T> generate(LLMRequest request, Class<T> responseClass) {

        List<PerplexityMessage> perplexityMessages = new ArrayList<>();

        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isBlank()) {
            perplexityMessages.add(PerplexityMessage.builder()
                    .role(ChatMessageRole.SYSTEM.name().toLowerCase())
                    .content(request.getSystemPrompt())
                    .build());
        }

        if (request.getMessages() != null) {
            request.getMessages().stream()
                    .map(msg -> PerplexityMessage.builder()
                            .role(msg.getRole().name().toLowerCase())
                            .content(msg.getContent())
                            .build())
                    .forEach(perplexityMessages::add);
        }

        PerplexityRequest perplexityRequest = PerplexityRequest.builder()
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

        log.info("Calling Perplexity LLM model={} format={} systemPrompt.len={} messages.size={}",
                perplexityProps.getModel(),
                request.getResponseFormat(),
                request.getSystemPrompt() == null ? 0 : request.getSystemPrompt().length(),
                request.getMessages() == null ? 0 : request.getMessages().size());
        log.debug("Perplexity request: baseUrl={} maxTokens={} temperature={}",
                perplexityProps.getBaseUrl(), perplexityProps.getMaxTokens(), perplexityProps.getTemperature());

        try {
            String response = webClient.post()
                    .uri(perplexityProps.getBaseUrl() + "/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + perplexityProps.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(perplexityRequest))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.debug("Perplexity raw response received len={}", response == null ? 0 : response.length());

            String content = extractContent(response);
            log.info("Perplexity response parsed successfully content.len={}", content == null ? 0 : content.length());
            return LLMResponseParser.parseResponse(content, responseClass);
        } catch (Exception e) {
            log.error("Error calling Perplexity API", e);
            throw new RuntimeException("Failed to get response from Perplexity: " + e.getMessage(), e);
        }
    }

    private String extractContent(String jsonResponse) {
        try {
            // Parse the response as a Map and extract choices[0].message.content
            Map<String, Object> responseMap = objectMapper.readValue(jsonResponse,
                    new TypeReference<Map<String, Object>>() {
                    });

            String content = "";
            Object choicesObj = responseMap.get("choices");
            if (choicesObj instanceof List<?> choices && !choices.isEmpty()) {
                Object firstChoiceObj = choices.get(0);
                if (firstChoiceObj instanceof Map<?, ?> firstChoice) {
                    Object messageObj = firstChoice.get("message");
                    if (messageObj instanceof Map<?, ?> message) {
                        Object contentObj = message.get("content");
                        if (contentObj != null) {
                            content = contentObj.toString();
                        }
                    }
                }
            }

            return content;
        } catch (Exception e) {
            log.error("Failed to parse response", e);
            throw new RuntimeException("Failed to parse LLM response: " + e.getMessage(), e);
        }
    }

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
    private static class PerplexityMessage {
        @JsonProperty("role")
        private String role;
        @JsonProperty("content")
        private String content;
    }
}
