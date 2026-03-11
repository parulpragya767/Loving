package com.lovingapp.config.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

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

        @Valid
        private Prompts prompts = new Prompts();

        @Data
        public static class Prompts {

            private Prompt empatheticChatResponse;
            private Prompt userContextExtraction;
            private Prompt wrapUpChatResponse;
        }

        @Getter
        @Setter
        public static class Prompt {
            private String name;
            private String id;
            private String version;
        }
    }
}
