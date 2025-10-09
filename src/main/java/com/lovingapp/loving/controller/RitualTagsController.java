package com.lovingapp.loving.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lovingapp.loving.service.RitualTagsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/tags")
public class RitualTagsController {
    private final RitualTagsService ritualTagsService;

    @GetMapping
    public Map<String, List<String>> allTags() {
        return ritualTagsService.getAllTags();
    }
}
