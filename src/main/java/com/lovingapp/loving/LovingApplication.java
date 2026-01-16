package com.lovingapp.loving;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LovingApplication {

    public static void main(String[] args) {
        SpringApplication.run(LovingApplication.class, args);
    }
}
