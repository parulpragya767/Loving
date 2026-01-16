package com.lovingapp.loving.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Validated
@ConfigurationProperties(prefix = "ai")
public class LlmClientProperties {

    /**
     * Select which provider to use (openai or perplexity)
     */
    private LlmProvider provider = LlmProvider.OPENAI;

    @Valid
    private OpenAiProperties openai = new OpenAiProperties();

    public enum LlmProvider {
        OPENAI
    }

    @Data
    public static class OpenAiProperties {

        @NotBlank(message = "OpenAI API key is required")
        private String apiKey;

        @NotBlank(message = "OpenAI model is required")
        private String model = "gpt-4.1-mini";
    }

}
