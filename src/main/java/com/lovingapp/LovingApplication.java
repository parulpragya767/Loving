package com.lovingapp;

import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LovingApplication {

    public static void main(String[] args) {
        String profile = Optional.ofNullable(System.getenv("SPRING_PROFILES_ACTIVE"))
                .orElse("dev");

        String filename = ".env" + (profile.equals("dev") ? "" : "." + profile);

        Dotenv dotenv = Dotenv.configure()
                .filename(filename)
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

        SpringApplication.run(LovingApplication.class, args);
    }
}
