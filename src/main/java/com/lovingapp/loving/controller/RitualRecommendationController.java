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
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/ritual-recommendation")
@Slf4j
public class RitualRecommendationController {

    private final RitualRecommendationService ritualRecommendationService;

    @GetMapping
    public ResponseEntity<List<RitualRecommendationDTO>> listAll(@CurrentUser UUID userId) {
        log.info("Fetch ritual recommendations request received");
        List<RitualRecommendationDTO> list = ritualRecommendationService.getAll(userId);
        log.info("Ritual recommendations fetched successfully count={}", list == null ? 0 : list.size());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RitualRecommendationDTO> listById(
            @CurrentUser UUID userId,
            @PathVariable("id") UUID id) {
        log.info("Fetch ritual recommendation request received recommendationId={}", id);
        RitualRecommendationDTO dto = ritualRecommendationService.getById(id);
        log.info("Ritual recommendation fetched successfully recommendationId={}", id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<RitualRecommendationDTO> create(
            @CurrentUser UUID userId,
            @RequestBody RitualRecommendationDTO request) {
        log.info("Create ritual recommendation request received");
        log.debug("Create ritual recommendation payload={}", request);
        RitualRecommendationDTO savedDto = ritualRecommendationService.create(userId, request);
        log.info("Ritual recommendation created successfully recommendationId={}", savedDto.getId());
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateRecommendationAndRitualHistoryStatus(
            @CurrentUser UUID userId,
            @PathVariable("id") UUID id,
            @RequestBody RitualRecommendationUpdateRequest request) {
        if (request.getStatus() == null) {
            log.info("Update ritual recommendation rejected: missing status recommendationId={}", id);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        log.info("Update ritual recommendation request received recommendationId={}", id);
        log.debug("Update ritual recommendation recommendationId={} payload={}", id, request);
        ritualRecommendationService.updateRecommendationAndRitualHistoryStatus(userId, id, request);
        log.info("Ritual recommendation updated successfully recommendationId={}", id);
        return ResponseEntity.ok().build();
    }
}
