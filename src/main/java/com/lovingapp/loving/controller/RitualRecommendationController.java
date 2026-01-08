package com.lovingapp.loving.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lovingapp.loving.auth.CurrentUser;
import com.lovingapp.loving.model.dto.RitualRecommendationDTOs.RitualRecommendationDTO;
import com.lovingapp.loving.model.dto.RitualRecommendationDTOs.RitualRecommendationUpdateRequest;
import com.lovingapp.loving.service.RitualRecommendationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/ritual-recommendation")
public class RitualRecommendationController {

    private final RitualRecommendationService ritualRecommendationService;

    @GetMapping
    public ResponseEntity<List<RitualRecommendationDTO>> listAll(@CurrentUser UUID userId) {
        List<RitualRecommendationDTO> list = ritualRecommendationService.getAll(userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RitualRecommendationDTO> listById(
            @CurrentUser UUID userId,
            @PathVariable("id") UUID id) {
        RitualRecommendationDTO dto = ritualRecommendationService.getById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<RitualRecommendationDTO> create(
            @CurrentUser UUID userId,
            @RequestBody RitualRecommendationDTO request) {
        RitualRecommendationDTO savedDto = ritualRecommendationService.create(userId, request);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateRecommendationAndRitualHistoryStatus(
            @CurrentUser UUID userId,
            @PathVariable("id") UUID id,
            @RequestBody RitualRecommendationUpdateRequest request) {
        if (request.getStatus() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ritualRecommendationService.updateRecommendationAndRitualHistoryStatus(userId, id, request);
        return ResponseEntity.ok().build();
    }
}
