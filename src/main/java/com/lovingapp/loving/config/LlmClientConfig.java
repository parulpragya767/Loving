package com.lovingapp.loving.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import com.lovingapp.loving.client.LlmClient;
import com.lovingapp.loving.client.OpenAiChatClient;
import com.lovingapp.loving.client.PerplexityLlmClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableConfigurationProperties(LlmClientProperties.class)
@RequiredArgsConstructor
@Slf4j
public class LlmClientConfig {

    private final LlmClientProperties properties;

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }

    @Bean
    public LlmClient llmClient(WebClient webClient) {
        String provider = properties.getProvider() == null ? "" : properties.getProvider().toLowerCase();
        log.info("LLM client provider configured provider={}", provider);
        return switch (provider) {
            case "perplexity" -> new PerplexityLlmClient(properties, webClient);
            case "openai" -> new OpenAiChatClient(properties);
            default -> {
                log.error("Unsupported LLM provider configured provider={}", properties.getProvider());
                throw new IllegalArgumentException("Unsupported LLM provider: " + properties.getProvider());
            }
        };
    }
}
