package com.lovingapp.loving.service.ai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Perplexity AI implementation of the LlmClient interface.
 */
@Component
public class PerplexityLlmClient implements LlmClient {

    private final LlmClientProperties.PerplexityProperties perplexityProps;
    private final WebClient webClient;

    public PerplexityLlmClient(LlmClientProperties llmClientProperties, WebClient webClient) {
        this.perplexityProps = llmClientProperties.getPerplexity();
        this.webClient = webClient;
    }

    @Override
    public Mono<String> chat(List<Message> messages) {
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
                .topP(0.9)  // Default value
                .topK(40)    // Default value
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
                .exchangeToMono(clientResponse -> {
                    // Log the response status and headers
                    System.out.println("Received response from Perplexity API:");
                    System.out.println("Status: " + clientResponse.statusCode());
                    System.out.println("Headers: " + clientResponse.headers().asHttpHeaders());
                    
                    // Read the response body as a string first for logging
                    return clientResponse.bodyToMono(String.class)
                            .doOnNext(body -> {
                                System.out.println("Response body: " + body);
                                try {
                                    // Try to parse it as JSON for better formatting
                                    ObjectMapper mapper = new ObjectMapper();
                                    Object json = mapper.readValue(body, Object.class);
                                    System.out.println("Formatted response: " + 
                                        mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
                                } catch (Exception e) {
                                    // If not JSON or parsing fails, just log as is
                                    System.out.println("Response body (raw): " + body);
                                }
                            })
                            .flatMap(body -> {
                                try {
                                    // Parse the response as a Map first to access fields directly
                                    ObjectMapper mapper = new ObjectMapper();
                                    Map<String, Object> responseMap = mapper.readValue(body, new TypeReference<Map<String, Object>>() {});
                                    
                                    // Get the first choice's message content directly
                                    if (responseMap.containsKey("choices")) {
                                        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                                        if (choices != null && !choices.isEmpty()) {
                                            Map<String, Object> firstChoice = choices.get(0);
                                            if (firstChoice.containsKey("message")) {
                                                Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                                                if (message != null && message.containsKey("content")) {
                                                    return Mono.just(message.get("content").toString());
                                                }
                                            }
                                        }
                                    }
                                    return Mono.error(new RuntimeException("No valid response content found in Perplexity AI response"));
                                } catch (Exception e) {
                                    return Mono.error(new RuntimeException("Failed to parse response: " + e.getMessage() + "\nResponse body: " + body));
                                }
                            });
                })
                .onErrorResume(e -> {
                    // Log the error for debugging
                    System.err.println("Error calling Perplexity API: " + e.getMessage());
                    e.printStackTrace();
                    return Mono.just("Sorry, I encountered an error while processing your request: " + e.getMessage());
                });
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
        private Integer searchContextSize;  // Make it Integer to handle potential null
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
