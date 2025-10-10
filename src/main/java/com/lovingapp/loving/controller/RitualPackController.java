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
}
