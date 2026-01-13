package com.lovingapp.loving.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.lovingapp.loving.model.dto.RitualDTO;
import com.lovingapp.loving.model.dto.RitualFilterDTO;
import com.lovingapp.loving.model.dto.RitualTagDTOs.RitualTags;
import com.lovingapp.loving.service.RitualService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/rituals")
@Slf4j
public class RitualController {

    private final RitualService ritualService;

    @GetMapping
    public List<RitualDTO> getAll() {
        log.info("Fetch rituals request received");
        List<RitualDTO> result = ritualService.getAllRituals();
        log.info("Rituals fetched successfully count={}", result == null ? 0 : result.size());
        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RitualDTO> getById(@PathVariable("id") UUID id) {
        log.info("Fetch ritual request received ritualId={}", id);
        try {
            RitualDTO ritual = ritualService.getRitualById(id);
            log.info("Ritual fetched successfully ritualId={}", id);
            return ResponseEntity.ok(ritual);
        } catch (Exception e) {
            log.error("Failed to fetch ritual ritualId={} message={}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @GetMapping("/tags")
    public RitualTags getAllTags() {
        log.info("Fetch ritual tags request received");
        RitualTags result = ritualService.getRitualTags();
        log.info("Ritual tags fetched successfully");
        return result;
    }

    @PostMapping("/search")
    public Page<RitualDTO> search(@RequestBody(required = false) RitualFilterDTO filter, Pageable pageable) {
        log.info("Ritual search request received");
        log.debug("Ritual search payload filter={} pageable={}", filter, pageable);
        if (filter == null) {
            filter = new RitualFilterDTO();
        }
        Page<RitualDTO> result = ritualService.searchRituals(filter, pageable);
        log.info("Ritual search completed successfully count={}", result == null ? 0 : result.getNumberOfElements());
        return result;
    }
}
