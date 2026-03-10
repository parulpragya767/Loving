package com.lovingapp.config.app;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "app.features")
public class FeatureFlagsProperties {

    private boolean paymentsEnabled;

    private boolean premiumEnabled;
}
