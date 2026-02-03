package com.lovingapp.loving.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lovingapp.loving.auth.CurrentUser;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.BulkRitualHistoryStatusUpdateRequest;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.RitualHistoryCreateRequest;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.RitualHistoryDTO;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.RitualHistoryUpdateRequest;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.StatusUpdateEntry;
import com.lovingapp.loving.model.dto.UserRitualsDTOs.CurrentRitualsDTO;
import com.lovingapp.loving.model.dto.UserRitualsDTOs.UserRitualDTO;
import com.lovingapp.loving.model.dto.UserRitualsDTOs.UserRitualPackDTO;
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
    public ResponseEntity<List<UserRitualDTO>> list(
            @CurrentUser UUID userId,
            @RequestParam(name = "status", required = false) RitualHistoryStatus status) {
        log.info("Fetch ritual history request received");

        List<UserRitualDTO> list = ritualHistoryService.listByUser(userId, status);

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

    @GetMapping("/recommendation/{recommendationId}")
    public ResponseEntity<UserRitualPackDTO> listByRecommendationId(@CurrentUser UUID userId,
            @PathVariable("recommendationId") UUID recommendationId) {
        log.info("Fetch ritual pack by recommendationId request received recommendationId={}", recommendationId);

        UserRitualPackDTO ritualPack = ritualHistoryService.listByRecommendationId(userId, recommendationId);

        log.info("Ritual pack by recommendationId fetched successfully");
        return ResponseEntity.ok(ritualPack);
    }

    @PostMapping
    public ResponseEntity<RitualHistoryDTO> create(@CurrentUser UUID userId,
            @Valid @RequestBody RitualHistoryCreateRequest request) {
        log.info("Create ritual history request received ritualId={}", request.getRitualId());

        RitualHistoryDTO savedDto = ritualHistoryService.create(userId, request);

        log.info("Ritual history created successfully ritualHistoryId={}", savedDto.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<RitualHistoryDTO>> bulkCreate(
            @CurrentUser UUID userId,
            @RequestBody List<@Valid RitualHistoryCreateRequest> ritualHistories) {
        log.info("Bulk creating ritual history request received ritualIds={}",
                ritualHistories.stream().map(RitualHistoryCreateRequest::getRitualId).collect(Collectors.toList()));

        List<RitualHistoryDTO> result = ritualHistoryService.bulkCreateRitualHistories(userId, ritualHistories);

        log.info("Ritual history created successfully (bulk) with {} records", result.size());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> complete(
            @CurrentUser UUID userId,
            @PathVariable("id") UUID id,
            @RequestBody RitualHistoryUpdateRequest request) {

        log.info("Complete ritual request received ritualHistoryId={}", id);

        ritualHistoryService.updateStatus(id, userId, RitualHistoryStatus.COMPLETED, request.getFeedback());

        log.info("Ritual completed successfully ritualHistoryId={}", id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @CurrentUser UUID userId,
            @PathVariable("id") UUID id,
            @Valid @RequestBody RitualHistoryUpdateRequest request) {

        log.info("Update ritual status request received ritualHistoryId={} status={}", id, request.getStatus());

        ritualHistoryService.updateStatus(id, userId, request.getStatus(), null);

        log.info("Ritual status updated successfully ritualHistoryId={}", id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/bulk/status")
    public ResponseEntity<Void> bulkUpdateStatus(
            @CurrentUser UUID userId,
            @Valid @RequestBody BulkRitualHistoryStatusUpdateRequest request) {

        log.info("Bulk status update request received count={} ritualHistoryIds={}", request.getUpdates().size(),
                request.getUpdates().stream().map(StatusUpdateEntry::getRitualHistoryId).collect(Collectors.toList()));

        ritualHistoryService.bulkUpdateStatus(userId, request.getUpdates());

        log.info("Bulk status update completed successfully.");
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @CurrentUser UUID userId,
            @PathVariable("id") UUID id) {

        log.info("Delete ritual history request received ritualHistoryId={}", id);

        // Doing a soft delete currently. Need to evaluate in future if a hard delete is
        // needed.
        ritualHistoryService.updateStatus(id, userId, RitualHistoryStatus.ABANDONED, null);

        log.info("Ritual history deleted successfully ritualHistoryId={}", id);
        return ResponseEntity.noContent().build();
    }
}
