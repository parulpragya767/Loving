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

import com.lovingapp.loving.dto.RitualPackDTO;
import com.lovingapp.loving.service.RitualPackService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/ritual-packs")
public class RitualPackController {

    private final RitualPackService ritualPackService;

    @GetMapping
    public List<RitualPackDTO> getAll() {
        return ritualPackService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RitualPackDTO> getById(@PathVariable("id") UUID id) {
        return ritualPackService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RitualPackDTO> create(@RequestBody RitualPackDTO request) {
        // Ignore client-provided id on create
        request.setId(null);
        RitualPackDTO created = ritualPackService.create(request);
        return ResponseEntity.created(URI.create("/api/ritual-packs/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RitualPackDTO> update(@PathVariable("id") UUID id, @RequestBody RitualPackDTO request) {
        return ritualPackService.update(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        boolean deleted = ritualPackService.deleteById(id);
        return deleted ? new ResponseEntity<>(HttpStatus.NO_CONTENT) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
