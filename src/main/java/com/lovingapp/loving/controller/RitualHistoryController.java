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
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/ritual-history")
@Slf4j
public class RitualHistoryController {

    private final RitualHistoryService ritualHistoryService;

    @GetMapping
    public ResponseEntity<List<RitualHistoryDTO>> list(@CurrentUser UUID userId) {
        log.info("Fetch ritual history request received");

        List<RitualHistoryDTO> list = ritualHistoryService.listByUser(userId);

        log.info("Ritual history fetched successfully count={}", list == null ? 0 : list.size());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/current")
    public ResponseEntity<CurrentRitualsDTO> listCurrent(@CurrentUser UUID userId) {
        log.info("Fetch current rituals request received");

        CurrentRitualsDTO currentRituals = ritualHistoryService.listCurrentByUser(userId);

        log.info("Current rituals fetched successfully");
        return ResponseEntity.ok(currentRituals);
    }

    @PostMapping
    public ResponseEntity<RitualHistoryDTO> create(@CurrentUser UUID userId,
            @Valid @RequestBody RitualHistoryCreateRequest request) {
        log.info("Create ritual history request received ritualId={}", request.getRitualId());

        RitualHistoryDTO savedDto = ritualHistoryService.create(userId, request);

        log.info("Ritual history created successfully ritualHistoryId={}", savedDto.getId());
        return ResponseEntity.status(201).body(savedDto);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<RitualHistoryDTO>> bulkCreate(
            @CurrentUser UUID userId,
            @Valid @RequestBody List<@Valid RitualHistoryCreateRequest> ritualHistories) {
        log.info("Bulk creating ritual history");
        log.debug("Bulk create ritual history payload: {}", ritualHistories);
        List<RitualHistoryDTO> result = ritualHistoryService.bulkCreateRitualHistories(userId, ritualHistories);
        log.info("Ritual history created successfully (bulk) with {} records", result.size());
        return ResponseEntity.status(201).body(result);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<RitualHistoryDTO> complete(
            @CurrentUser UUID userId,
            @PathVariable("id") UUID id,
            @RequestBody RitualHistoryUpdateRequest request) {
        log.info("Complete ritual request received ritualHistoryId={}", id);
        log.debug("Complete ritual payload ritualHistoryId={} payload={}", id, request);
        return ResponseEntity.ok(ritualHistoryService
                .updateStatus(id, userId, RitualHistoryStatus.COMPLETED, request.getFeedback()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<RitualHistoryDTO> updateStatus(
            @CurrentUser UUID userId,
            @PathVariable("id") UUID id,
            @RequestBody RitualHistoryUpdateRequest request) {
        if (request.getStatus() == null) {
            log.info("Update ritual status rejected: missing status ritualHistoryId={}", id);
            return ResponseEntity.badRequest().build();
        }
        log.info("Update ritual status request received ritualHistoryId={} status={}", id, request.getStatus());
        return ResponseEntity.ok(ritualHistoryService
                .updateStatus(id, userId, request.getStatus(), null));
    }

    @PutMapping("/bulk/status")
    public ResponseEntity<List<RitualHistoryDTO>> bulkUpdateStatus(
            @CurrentUser UUID userId,
            @Valid @RequestBody BulkRitualHistoryStatusUpdateRequest request) {
        if (request.getUpdates() == null || request.getUpdates().isEmpty()) {
            log.info("Bulk status update rejected: no updates provided");
            return ResponseEntity.badRequest().build();
        }

        log.info("Bulk status update request received count={}", request.getUpdates().size());
        log.debug("Bulk status update payload: {}", request);

        List<RitualHistoryDTO> updatedHistories = ritualHistoryService.bulkUpdateStatus(
                userId,
                request.getUpdates());

        log.info("Bulk status update completed successfully count={}",
                updatedHistories == null ? 0 : updatedHistories.size());
        return ResponseEntity.ok(updatedHistories);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @CurrentUser UUID userId,
            @PathVariable("id") UUID id) {
        log.info("Abandon ritual request received ritualHistoryId={}", id);
        ritualHistoryService
                .updateStatus(id, userId, RitualHistoryStatus.ABANDONED, null);
        log.info("Ritual abandoned successfully ritualHistoryId={}", id);
        return ResponseEntity.noContent().build();
    }
}
