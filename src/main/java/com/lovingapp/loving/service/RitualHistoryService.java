package com.lovingapp.loving.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.loving.mapper.RitualHistoryMapper;
import com.lovingapp.loving.model.dto.BulkRitualHistoryStatusUpdateRequest.RitualStatusUpdate;
import com.lovingapp.loving.model.dto.CurrentRitualsDTO;
import com.lovingapp.loving.model.dto.RitualDTO;
import com.lovingapp.loving.model.dto.RitualHistoryDTO;
import com.lovingapp.loving.model.dto.RitualPackDTO;
import com.lovingapp.loving.model.entity.RitualHistory;
import com.lovingapp.loving.model.enums.EmojiFeedback;
import com.lovingapp.loving.model.enums.RitualHistoryStatus;
import com.lovingapp.loving.repository.RitualHistoryRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RitualHistoryService {

    private final RitualHistoryRepository ritualHistoryRepository;
    private final RitualPackService ritualPackService;
    private final RitualService ritualService;

    public List<RitualHistoryDTO> listByUser(UUID userId) {
        return ritualHistoryRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(RitualHistoryMapper::toDto)
                .collect(Collectors.toList());
    }

    public CurrentRitualsDTO listCurrentByUser(UUID userId) {
        // Get all active rituals for the user
        List<RitualHistory> activeRituals = ritualHistoryRepository
                .findByUserIdAndStatusInOrderByUpdatedAtDesc(userId, List.of(RitualHistoryStatus.ACTIVE));

        // Extract unique ritual and ritual pack IDs
        Set<UUID> ritualIds = new HashSet<>();
        Set<UUID> standaloneRitualIds = new HashSet<>();
        Set<UUID> ritualPackIds = new HashSet<>();

        // Map to track the rituals inside a ritual pack
        Map<UUID, Set<UUID>> ritualPackToRituals = new HashMap<>();

        // Process active rituals to collect IDs and build relationships
        for (RitualHistory history : activeRituals) {
            UUID ritualId = history.getRitualId();
            UUID packId = history.getRitualPackId();

            ritualIds.add(ritualId);

            if (packId != null) {
                ritualPackIds.add(packId);
                ritualPackToRituals.computeIfAbsent(packId, k -> new HashSet<>()).add(ritualId);
            } else {
                standaloneRitualIds.add(ritualId);
            }
        }

        // Fetch all related rituals and ritual packs
        Map<UUID, RitualDTO> ritualsMap = ritualService.findAllById(new ArrayList<>(ritualIds)).stream()
                .collect(Collectors.toMap(RitualDTO::getId, Function.identity()));

        Map<UUID, RitualPackDTO> ritualPacksMap = ritualPackService.findAllById(new ArrayList<>(ritualPackIds)).stream()
                .collect(Collectors.toMap(RitualPackDTO::getId, Function.identity()));

        // Create updated ritual packs with only active rituals
        for (UUID packId : ritualPacksMap.keySet()) {
            RitualPackDTO ritualPack = ritualPacksMap.get(packId);
            List<RitualDTO> rituals = ritualPackToRituals
                    .getOrDefault(packId, Collections.emptySet())
                    .stream()
                    .map(ritualsMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            ritualPack.setRituals(rituals);
        }

        // Get all active rituals that are not part of any pack
        List<RitualDTO> standaloneRituals = standaloneRitualIds
                .stream()
                .map(ritualsMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return CurrentRitualsDTO.builder()
                .ritualHistory(activeRituals.stream()
                        .map(RitualHistoryMapper::toDto)
                        .collect(Collectors.toList()))
                .ritualPacks(ritualPacksMap.values().stream().collect(Collectors.toList()))
                .rituals(standaloneRituals)
                .build();
    }

    public Optional<RitualHistoryDTO> findById(UUID id) {
        return ritualHistoryRepository.findById(id)
                .map(RitualHistoryMapper::toDto);
    }

    @Transactional
    public RitualHistoryDTO save(UUID userId, RitualHistoryDTO entity) {
        // Check if the ritual exists
        RitualDTO ritual = ritualService.getRitualById(entity.getRitualId());
        if (ritual == null) {
            throw new EntityNotFoundException("Ritual not found with id: " + entity.getRitualId());
        }

        RitualHistory ritualHistory = RitualHistoryMapper.fromDto(entity);
        ritualHistory.setUserId(userId);
        RitualHistory saved = ritualHistoryRepository.save(ritualHistory);
        return RitualHistoryMapper.toDto(saved);
    }

    @Transactional
    public void delete(RitualHistoryDTO entity) {
        RitualHistory ritualHistory = RitualHistoryMapper.fromDto(entity);
        ritualHistoryRepository.delete(ritualHistory);
    }

    @Transactional
    public List<RitualHistoryDTO> bulkCreateRitualHistories(UUID userId, List<RitualHistoryDTO> ritualHistories) {
        // Validate all ritual IDs exist
        Set<UUID> ritualIds = ritualHistories.stream()
                .map(RitualHistoryDTO::getRitualId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (!ritualIds.isEmpty()) {
            List<UUID> existingRituals = ritualService.findAllById(new ArrayList<>(ritualIds)).stream()
                    .map(RitualDTO::getId)
                    .collect(Collectors.toList());

            List<UUID> missingRituals = ritualIds.stream()
                    .filter(id -> !existingRituals.contains(id))
                    .collect(Collectors.toList());

            if (!missingRituals.isEmpty()) {
                throw new EntityNotFoundException("The following ritual IDs were not found: " + missingRituals);
            }
        }

        // Validate all ritual pack IDs exist if provided
        Set<UUID> packIds = ritualHistories.stream()
                .map(RitualHistoryDTO::getRitualPackId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (!packIds.isEmpty()) {
            List<UUID> existingPacks = ritualPackService.findAllById(new ArrayList<>(packIds)).stream()
                    .map(RitualPackDTO::getId)
                    .collect(Collectors.toList());

            List<UUID> missingPacks = packIds.stream()
                    .filter(id -> !existingPacks.contains(id))
                    .collect(Collectors.toList());

            if (!missingPacks.isEmpty()) {
                throw new EntityNotFoundException("The following ritual pack IDs were not found: " + missingPacks);
            }
        }

        // Map DTOs to entities and set the user ID
        List<RitualHistory> histories = ritualHistories.stream()
                .map(dto -> {
                    RitualHistory history = RitualHistoryMapper.fromDto(dto);
                    history.setUserId(userId);
                    return history;
                })
                .collect(Collectors.toList());

        // Save all in a single batch
        List<RitualHistory> savedHistories = ritualHistoryRepository.saveAll(histories);

        // Convert back to DTOs and return
        return savedHistories.stream()
                .map(RitualHistoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<RitualHistoryDTO> addRitualPack(UUID userId, UUID ritualPackId) {
        // Find the ritual pack
        RitualPackDTO ritualPack = ritualPackService.findById(ritualPackId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Ritual pack not found with id: " + ritualPackId));

        // Get all rituals in the pack
        List<RitualDTO> rituals = ritualPack.getRituals();
        if (rituals == null || rituals.isEmpty()) {
            return Collections.emptyList();
        }

        // Create new history entries for all rituals in the pack
        List<RitualHistory> histories = rituals.stream()
                .map(ritual -> RitualHistory.builder()
                        .ritualId(ritual.getId())
                        .ritualPackId(ritualPackId)
                        .userId(userId)
                        .status(RitualHistoryStatus.SUGGESTED)
                        .build())
                .collect(Collectors.toList());

        // Save all new histories in a single database call
        List<RitualHistory> savedHistories = ritualHistoryRepository.saveAll(histories);

        // Convert to DTOs and return
        return savedHistories.stream()
                .map(RitualHistoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<RitualHistoryDTO> updateStatusWithOwnership(UUID id, UUID userId, RitualHistoryStatus status,
            EmojiFeedback feedback) {
        Optional<RitualHistory> found = ritualHistoryRepository.findById(id)
                .filter(rh -> rh.getUserId().equals(userId));
        if (found.isEmpty()) {
            return Optional.empty();
        }
        RitualHistory rh = found.get();
        rh.setStatus(status);
        if (feedback != null) {
            rh.setFeedback(feedback);
        }
        RitualHistory saved = ritualHistoryRepository.save(rh);
        return Optional.ofNullable(RitualHistoryMapper.toDto(saved));
    }

    @Transactional
    public List<RitualHistoryDTO> bulkUpdateStatusWithOwnership(UUID userId, List<RitualStatusUpdate> updates) {

        // Get all ritual history IDs from the updates
        List<UUID> ritualHistoryIds = updates.stream()
                .map(RitualStatusUpdate::getRitualHistoryId)
                .collect(Collectors.toList());

        // Find all ritual histories that belong to the user
        Map<UUID, RitualHistory> historyMap = ritualHistoryRepository.findAllById(ritualHistoryIds).stream()
                .filter(rh -> rh.getUserId().equals(userId))
                .collect(Collectors.toMap(RitualHistory::getId, rh -> rh));

        // Create a map of updates by ritual history ID for quick lookup
        Map<UUID, RitualStatusUpdate> updatesMap = updates.stream()
                .collect(Collectors.toMap(
                        RitualStatusUpdate::getRitualHistoryId,
                        update -> update));

        // Update each history with its corresponding status and feedback
        List<RitualHistory> historiesToUpdate = new ArrayList<>();
        for (Map.Entry<UUID, RitualHistory> entry : historyMap.entrySet()) {
            UUID historyId = entry.getKey();
            RitualHistory history = entry.getValue();
            RitualStatusUpdate update = updatesMap.get(historyId);

            if (update != null) {
                history.setStatus(update.getStatus());
                historiesToUpdate.add(history);
            }
        }

        // Save all updates
        List<RitualHistory> savedHistories = ritualHistoryRepository.saveAll(historiesToUpdate);

        // Convert to DTOs and return
        return savedHistories.stream()
                .map(RitualHistoryMapper::toDto)
                .collect(Collectors.toList());
    }
}
