package com.lovingapp.loving.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ai")
public class LlmClientProperties {
    
    /**
     * Select which provider to use (openai or perplexity)
     */
    private String provider = "perplexity";
    
    private OpenAiProperties openai = new OpenAiProperties();
    private PerplexityProperties perplexity = new PerplexityProperties();

    @Data
    public static class OpenAiProperties {
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

    @Data
    public static class PerplexityProperties {
        /**
         * Perplexity API key (required)
         */
        private String apiKey;
        
        /**
         * Base URL for Perplexity API
         */
        private String baseUrl = "https://api.perplexity.ai";
        
        /**
         * Model to use (e.g., pplx-70b-online, pplx-7b-online)
         */
        private String model = "pplx-7b-online";
        
        /**
         * Maximum number of tokens to generate
         */
        private Integer maxTokens = 1000;
        
        /**
         * Sampling temperature (0.0 to 1.0)
         */
        private Double temperature = 0.7;
    }
}
