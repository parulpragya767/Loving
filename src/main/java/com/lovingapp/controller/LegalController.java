package com.lovingapp.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/legal")
@Slf4j
public class LegalController {

    @GetMapping(value = "/terms-and-conditions", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getTermsAndConditions() throws IOException {
        log.info("Terms and Conditions request received");

        Resource resource = new ClassPathResource("static/terms-and-conditions.html");
        String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);

        log.info("Terms and Conditions served successfully");
        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }

    @GetMapping(value = "/privacy-policy", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getPrivacyPolicy() throws IOException {
        log.info("Privacy Policy request received");

        Resource resource = new ClassPathResource("static/privacy-policy.html");
        String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);

        log.info("Privacy Policy served successfully");
        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }
}
