package com.lovingapp.loving.config;

import com.lovingapp.loving.service.ai.LlmClient;
import com.lovingapp.loving.service.ai.OpenAiChatClient;
import com.lovingapp.loving.service.ai.PerplexityLlmClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class LlmClientConfig {

    private final LlmClientProperties properties;

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }

    @Bean
    public LlmClient llmClient(WebClient webClient) {
        return switch (properties.getProvider().toLowerCase()) {
            case "perplexity" -> new PerplexityLlmClient(properties, webClient);
            case "openai" -> new OpenAiChatClient(properties, webClient);
            default -> throw new IllegalArgumentException("Unsupported LLM provider: " + properties.getProvider());
        };
    }
}
