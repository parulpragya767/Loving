package com.lovingapp.loving.client;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lovingapp.loving.config.LlmClientProperties;
import com.lovingapp.loving.helpers.ai.LLMResponseParser;
import com.lovingapp.loving.model.domain.ai.LLMRequest;
import com.lovingapp.loving.model.domain.ai.LLMResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenAI implementation of the LlmClient interface.
 */
@Slf4j
@Component
public class OpenAiChatClient implements LlmClient {

    private final LlmClientProperties.OpenAiProperties openAiProps;
    private final WebClient webClient;
    private final LLMResponseParser llmResponseParser;

    public OpenAiChatClient(LlmClientProperties llmClientProperties,
            WebClient webClient,
            LLMResponseParser llmResponseParser) {
        this.openAiProps = llmClientProperties.getOpenai();
        this.webClient = webClient;
        this.llmResponseParser = llmResponseParser;
    }

    @Override
    public LLMResponse generate(LLMRequest request) {
        OpenAiRequest openAiRequest = OpenAiRequest.builder()
                .model(openAiProps.getModel())
                .maxTokens(openAiProps.getMaxTokens())
                .temperature(openAiProps.getTemperature())
                .messages(List.of(OpenAiMessage.builder().role("user").content(request.getSystemPrompt()).build()))
                .build();

        try {
            String jsonResponse = webClient.post()
                    .uri(openAiProps.getBaseUrl() + "/v1/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiProps.getApiKey())
                    .body(BodyInserters.fromValue(openAiRequest))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Use the LLMResponseParser to parse the response
            return llmResponseParser.parseResponse(jsonResponse);
        } catch (Exception e) {
            log.error("Error calling OpenAI API", e);
            throw new RuntimeException("Failed to get response from OpenAI", e);
        }
    }

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
        private List<OpenAiMessage> messages;
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
