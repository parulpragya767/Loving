package com.lovingapp.loving.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

import com.lovingapp.loving.model.dto.CurrentRitualsDTO;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.BulkRitualHistoryStatusUpdateRequest;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.RitualHistoryDTO;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.RitualHistoryUpdateRequest;
import com.lovingapp.loving.model.enums.RitualHistoryStatus;
import com.lovingapp.loving.service.RitualHistoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/ritual-history")
public class RitualHistoryController {

    private final RitualHistoryService ritualHistoryService;

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
    public ResponseEntity<List<RitualHistoryDTO>> list(
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = getAuthUserId(jwt);
        List<RitualHistoryDTO> list = ritualHistoryService.listByUser(userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/current")
    public ResponseEntity<CurrentRitualsDTO> listCurrent(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = getAuthUserId(jwt);
        CurrentRitualsDTO currentRituals = ritualHistoryService.listCurrentByUser(userId);
        return ResponseEntity.ok(currentRituals);
    }

    @PostMapping
    public ResponseEntity<RitualHistoryDTO> create(@AuthenticationPrincipal Jwt jwt,
            @RequestBody RitualHistoryDTO request) {
        UUID userId = getAuthUserId(jwt);
        RitualHistoryDTO savedDto = ritualHistoryService.create(userId, request);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<RitualHistoryDTO>> bulkCreate(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody List<@Valid RitualHistoryDTO> ritualHistories) {
        UUID userId = getAuthUserId(jwt);

        List<RitualHistoryDTO> result = ritualHistoryService.bulkCreateRitualHistories(userId, ritualHistories);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<RitualHistoryDTO> complete(@AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID id,
            @RequestBody RitualHistoryUpdateRequest request) {
        UUID userId = getAuthUserId(jwt);
        return ResponseEntity.ok(ritualHistoryService
                .updateStatus(id, userId, RitualHistoryStatus.COMPLETED, request.getFeedback()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<RitualHistoryDTO> updateStatus(@AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID id,
            @RequestBody RitualHistoryUpdateRequest request) {
        UUID userId = getAuthUserId(jwt);
        if (request.getStatus() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(ritualHistoryService
                .updateStatus(id, userId, request.getStatus(), null));
    }

    @PutMapping("/bulk/status")
    public ResponseEntity<List<RitualHistoryDTO>> bulkUpdateStatus(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody BulkRitualHistoryStatusUpdateRequest request) {
        UUID userId = getAuthUserId(jwt);

        if (request.getUpdates() == null || request.getUpdates().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<RitualHistoryDTO> updatedHistories = ritualHistoryService.bulkUpdateStatus(
                userId,
                request.getUpdates());

        return ResponseEntity.ok(updatedHistories);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") UUID id) {
        UUID userId = getAuthUserId(jwt);
        ritualHistoryService
                .updateStatus(id, userId, RitualHistoryStatus.ABANDONED, null);
        return ResponseEntity.noContent().build();
    }
}
