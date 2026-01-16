package com.lovingapp.loving;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lovingapp.loving.config.database.SupabaseProperties;
import com.lovingapp.loving.config.llm.LlmClientProperties;

@SpringBootApplication
@EnableConfigurationProperties({ LlmClientProperties.class, SupabaseProperties.class })
@RestController
public class LovingApplication {

    public static void main(String[] args) {
        SpringApplication.run(LovingApplication.class, args);
    }

    @GetMapping("/sayHello")
    public String sayHello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return String.format("Hello %s!", name);
    }
}
