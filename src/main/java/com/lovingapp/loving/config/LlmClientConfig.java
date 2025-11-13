package com.lovingapp.loving.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import com.lovingapp.loving.client.LlmClient;
import com.lovingapp.loving.client.OpenAiChatClient;
import com.lovingapp.loving.client.PerplexityLlmClient;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableConfigurationProperties(LlmClientProperties.class)
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
            case "openai" -> new OpenAiChatClient(properties);
            default -> throw new IllegalArgumentException("Unsupported LLM provider: " + properties.getProvider());
        };
    }
}
