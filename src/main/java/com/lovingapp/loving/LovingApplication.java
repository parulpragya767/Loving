package com.lovingapp.loving;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.lovingapp.loving.config.LlmClientProperties;
import com.lovingapp.loving.config.SupabaseProperties;

@SpringBootApplication
@EnableConfigurationProperties({LlmClientProperties.class, SupabaseProperties.class})
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
