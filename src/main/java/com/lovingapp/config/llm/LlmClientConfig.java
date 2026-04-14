package com.lovingapp.config.llm;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.lovingapp.client.LlmClient;
import com.lovingapp.client.OpenAiChatClient;
import com.lovingapp.service.MetricsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableConfigurationProperties(LlmClientProperties.class)
@RequiredArgsConstructor
@Slf4j
public class LlmClientConfig {

    private final LlmClientProperties properties;
    private final MetricsService metricsService;

    @Bean
    public LlmClient llmClient() {
        log.info("LLM provider configured provider={}", properties.getProvider());

        return switch (properties.getProvider()) {
            case OPENAI -> new OpenAiChatClient(properties.getOpenai(), metricsService);
            default -> throw new IllegalArgumentException("Unsupported LLM provider: " + properties.getProvider());

        };
    }
}
