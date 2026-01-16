package com.lovingapp.loving.infra.startup;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.lovingapp.loving.service.ContentManagementService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContentSyncBootstrap implements ApplicationRunner {

    private final ContentManagementService contentManagementService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting content synchronization");

        contentManagementService.syncAll();

        log.info("Content synchronization completed");
    }
}