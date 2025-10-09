package com.lovingapp.loving.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.lovingapp.loving.dto.RitualDTO;
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

    @PostMapping
    public ResponseEntity<RitualDTO> create(@RequestBody RitualDTO request) {
        RitualDTO createdRitual = ritualService.createRitual(request);
        return ResponseEntity
                .created(URI.create("/api/rituals/" + createdRitual.getId()))
                .body(createdRitual);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RitualDTO> update(@PathVariable("id") UUID id, @RequestBody RitualDTO request) {
        try {
            RitualDTO updatedRitual = ritualService.updateRitual(id, request);
            return ResponseEntity.ok(updatedRitual);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        try {
            ritualService.deleteRitual(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }
}
