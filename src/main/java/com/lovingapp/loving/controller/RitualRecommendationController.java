package com.lovingapp.loving.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

    private UUID getAuthUserId(Jwt jwt) {
        String sub = jwt.getSubject();
        if (sub == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not present");
        }
        try {
            return UUID.fromString(sub);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user id in token");
        }
    }

    @GetMapping
    public ResponseEntity<List<RitualRecommendationDTO>> listAll(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = getAuthUserId(jwt);
        List<RitualRecommendationDTO> list = ritualRecommendationService.getAll(userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RitualRecommendationDTO> listById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID id) {
        getAuthUserId(jwt);
        RitualRecommendationDTO dto = ritualRecommendationService.getById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<RitualRecommendationDTO> create(@AuthenticationPrincipal Jwt jwt,
            @RequestBody RitualRecommendationDTO request) {
        UUID userId = getAuthUserId(jwt);
        RitualRecommendationDTO savedDto = ritualRecommendationService.create(userId, request);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateRecommendationAndRitualHistoryStatus(@AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID id,
            @RequestBody RitualRecommendationUpdateRequest request) {
        UUID userId = getAuthUserId(jwt);
        if (request.getStatus() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ritualRecommendationService.updateRecommendationAndRitualHistoryStatus(userId, id, request);
        return ResponseEntity.ok().build();
    }
}
