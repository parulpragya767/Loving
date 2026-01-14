package com.lovingapp.loving.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lovingapp.loving.model.dto.RitualPackDTO;
import com.lovingapp.loving.service.RitualPackService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/ritual-packs")
@Slf4j
public class RitualPackController {

    private final RitualPackService ritualPackService;

    @GetMapping
    public List<RitualPackDTO> getAll() {
        log.info("Fetch ritual packs request received");

        List<RitualPackDTO> result = ritualPackService.findAll();

        log.info("Ritual packs fetched successfully count={}", result == null ? 0 : result.size());
        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RitualPackDTO> getById(@PathVariable("id") UUID id) {
        log.info("Fetch ritual pack request received ritualPackId={}", id);

        RitualPackDTO result = ritualPackService.findById(id);

        log.info("Ritual pack fetch completed ritualPackId={}", id);
        return ResponseEntity.ok(result);
    }
}
