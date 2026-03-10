package com.lovingapp.config.app;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "app.usage")
public class AppUsageLimitsProperties {

    private Limits free = new Limits();
    private Limits premium = new Limits();

    @Data
    public static class Limits {
        private int dailyAiMessages;
        private int weeklyRecommendations;
    }
}
