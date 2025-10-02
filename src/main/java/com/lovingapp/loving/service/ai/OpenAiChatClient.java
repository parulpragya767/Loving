package com.lovingapp.loving.service.ai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lovingapp.loving.config.LlmClientProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * OpenAI implementation of the LlmClient interface.
 */
@Component
public class OpenAiChatClient implements LlmClient {

    private final LlmClientProperties.OpenAiProperties openAiProps;
    private final WebClient webClient;

    public OpenAiChatClient(LlmClientProperties llmClientProperties, WebClient webClient) {
        this.openAiProps = llmClientProperties.getOpenai();
        this.webClient = webClient;
    }

    @Override
    public Mono<String> chat(List<Message> messages) {
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
                .bodyToMono(OpenAiResponse.class)
                .map(r -> r.getChoices() != null && !r.getChoices().isEmpty()
                        ? r.getChoices().get(0).getMessage().getContent()
                        : "");
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
