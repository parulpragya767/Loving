package com.lovingapp.loving.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai.openai")
public class AiClientProperties {
    private String apiKey;         // set via env or application.yml: ai.openai.api-key
    private String baseUrl = "https://api.openai.com"; // override for compatible providers
    private String model = "gpt-4o-mini"; // default lightweight model
    private Integer maxTokens = 600;
    private Double temperature = 0.3;
}
