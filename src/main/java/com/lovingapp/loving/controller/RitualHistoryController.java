package com.lovingapp.loving.controller;

import java.util.List;
import java.util.UUID;

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

import com.lovingapp.loving.auth.CurrentUser;
import com.lovingapp.loving.model.dto.CurrentRitualDTOs.CurrentRitualsDTO;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.BulkRitualHistoryStatusUpdateRequest;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.RitualHistoryCreateRequest;
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

    @GetMapping
    public ResponseEntity<List<RitualHistoryDTO>> list(@CurrentUser UUID userId) {
        List<RitualHistoryDTO> list = ritualHistoryService.listByUser(userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/current")
    public ResponseEntity<CurrentRitualsDTO> listCurrent(@CurrentUser UUID userId) {
        CurrentRitualsDTO currentRituals = ritualHistoryService.listCurrentByUser(userId);
        return ResponseEntity.ok(currentRituals);
    }

    @PostMapping
    public ResponseEntity<RitualHistoryDTO> create(@CurrentUser UUID userId,
            @RequestBody RitualHistoryCreateRequest request) {
        RitualHistoryDTO savedDto = ritualHistoryService.create(userId, request);
        return ResponseEntity.status(201).body(savedDto);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<RitualHistoryDTO>> bulkCreate(
            @CurrentUser UUID userId,
            @Valid @RequestBody List<@Valid RitualHistoryCreateRequest> ritualHistories) {
        List<RitualHistoryDTO> result = ritualHistoryService.bulkCreateRitualHistories(userId, ritualHistories);
        return ResponseEntity.status(201).body(result);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<RitualHistoryDTO> complete(
            @CurrentUser UUID userId,
            @PathVariable("id") UUID id,
            @RequestBody RitualHistoryUpdateRequest request) {
        return ResponseEntity.ok(ritualHistoryService
                .updateStatus(id, userId, RitualHistoryStatus.COMPLETED, request.getFeedback()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<RitualHistoryDTO> updateStatus(
            @CurrentUser UUID userId,
            @PathVariable("id") UUID id,
            @RequestBody RitualHistoryUpdateRequest request) {
        if (request.getStatus() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(ritualHistoryService
                .updateStatus(id, userId, request.getStatus(), null));
    }

    @PutMapping("/bulk/status")
    public ResponseEntity<List<RitualHistoryDTO>> bulkUpdateStatus(
            @CurrentUser UUID userId,
            @Valid @RequestBody BulkRitualHistoryStatusUpdateRequest request) {
        if (request.getUpdates() == null || request.getUpdates().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<RitualHistoryDTO> updatedHistories = ritualHistoryService.bulkUpdateStatus(
                userId,
                request.getUpdates());

        return ResponseEntity.ok(updatedHistories);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @CurrentUser UUID userId,
            @PathVariable("id") UUID id) {
        ritualHistoryService
                .updateStatus(id, userId, RitualHistoryStatus.ABANDONED, null);
        return ResponseEntity.noContent().build();
    }
}
