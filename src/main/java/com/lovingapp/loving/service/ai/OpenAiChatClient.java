package com.lovingapp.loving.service.ai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lovingapp.loving.config.AiClientProperties;
import com.lovingapp.loving.model.ai.ChatMessageRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OpenAiChatClient {

    private final AiClientProperties props;

    public Mono<String> chat(List<Message> messages) {
        WebClient client = WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        ChatRequest req = new ChatRequest();
        req.setModel(props.getModel());
        req.setMaxTokens(props.getMaxTokens());
        req.setTemperature(props.getTemperature());
        req.setMessages(messages);

        return client.post()
                .uri("/v1/chat/completions")
                .body(BodyInserters.fromValue(req))
                .retrieve()
                .bodyToMono(ChatResponse.class)
                .map(r -> r.getChoices() != null && !r.getChoices().isEmpty()
                        ? r.getChoices().get(0).getMessage().getContent()
                        : "");
    }

    // Helper structures for API
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ChatRequest {
        private String model;
        @JsonProperty("max_tokens")
        private Integer maxTokens;
        private Double temperature;
        private List<Message> messages = new ArrayList<>();
    }

    @Data
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Message {
        private String role;
        private String content;
        public static Message of(ChatMessageRole role, String content) {
            return new Message(role.name().toLowerCase(), content);
        }
    }

    @Data
    public static class ChatResponse {
        private List<Choice> choices;
    }

    @Data
    public static class Choice {
        private int index;
        private AIMessage message;
    }

    @Data
    public static class AIMessage {
        private String role;
        private String content;
    }
}
