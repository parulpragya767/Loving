package com.lovingapp.loving.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import com.lovingapp.loving.client.LlmClient;
import com.lovingapp.loving.client.OpenAiChatClient;
import com.lovingapp.loving.client.PerplexityLlmClient;
import com.lovingapp.loving.helpers.ai.LLMResponseParser;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class LlmClientConfig {

    private final LlmClientProperties properties;

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }

    @Bean
    public LLMResponseParser llmResponseParser() {
        return new LLMResponseParser();
    }

    @Bean
    public LlmClient llmClient(WebClient webClient, LLMResponseParser llmResponseParser) {
        return switch (properties.getProvider().toLowerCase()) {
            case "perplexity" -> new PerplexityLlmClient(properties, webClient, llmResponseParser);
            case "openai" -> new OpenAiChatClient(properties, webClient, llmResponseParser);
            default -> throw new IllegalArgumentException("Unsupported LLM provider: " + properties.getProvider());
        };
    }
}
