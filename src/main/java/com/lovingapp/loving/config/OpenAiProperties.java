package com.lovingapp.loving.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for OpenAI client.
 * Configure in application.yml with prefix: ai.openai
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai.openai")
public class OpenAiProperties {
    /**
     * OpenAI API key (required)
     */
    private String apiKey;
    
    /**
     * Base URL for OpenAI API
     */
    private String baseUrl = "https://api.openai.com";
    
    /**
     * Model to use (e.g., gpt-4, gpt-3.5-turbo)
     */
    private String model = "gpt-4";
    
    /**
     * Maximum number of tokens to generate
     */
    private Integer maxTokens = 1000;
    
    /**
     * Sampling temperature (0.0 to 2.0)
     */
    private Double temperature = 0.7;
}
