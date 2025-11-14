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

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/rituals")
public class RitualController {

    private final RitualService ritualService;

    @GetMapping
    public List<RitualDTO> getAll() {
        return ritualService.getAllRituals();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RitualDTO> getById(@PathVariable("id") UUID id) {
        try {
            RitualDTO ritual = ritualService.getRitualById(id);
            return ResponseEntity.ok(ritual);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @GetMapping("/tags")
    public RitualTags getAllTags() {
        return ritualService.getRitualTags();
    }

    @PostMapping("/search")
    public Page<RitualDTO> search(@RequestBody(required = false) RitualFilterDTO filter, Pageable pageable) {
        if (filter == null) {
            filter = new RitualFilterDTO();
        }
        return ritualService.searchRituals(filter, pageable);
    }
}
