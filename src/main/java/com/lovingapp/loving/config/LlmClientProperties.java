package com.lovingapp.loving.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "ai")
public class LlmClientProperties {

    /**
     * Select which provider to use (openai or perplexity)
     */
    private String provider = "openai";

    private OpenAiProperties openai = new OpenAiProperties();

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
        private String model = "gpt-4.1-mini";

        /**
         * Maximum number of tokens to generate
         */
        private Integer maxTokens = 2000;

        /**
         * Sampling temperature (0.0 to 2.0)
         */
        private Double temperature = 0.7;
    }

}
