package com.lovingapp.loving.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.lovingapp.loving.mapper.RitualHistoryMapper;
import com.lovingapp.loving.model.dto.CurrentRitualsDTO;
import com.lovingapp.loving.model.dto.RitualDTO;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.RitualHistoryDTO;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.StatusUpdateEntry;
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
                List<RitualHistory> activeRituals = ritualHistoryRepository
                                .findByUserIdAndStatusInOrderByUpdatedAtDesc(userId,
                                                List.of(RitualHistoryStatus.ACTIVE, RitualHistoryStatus.STARTED));

                Map<UUID, List<RitualHistoryDTO>> ritualHistoryMap = activeRituals.stream()
                                .map(RitualHistoryMapper::toDto)
                                .collect(Collectors.groupingBy(RitualHistoryDTO::getRitualId));

                Set<UUID> ritualPackIds = activeRituals.stream()
                                .map(RitualHistory::getRitualPackId)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet());

                Map<UUID, Set<UUID>> ritualPackToRituals = activeRituals.stream()
                                .filter(rh -> rh.getRitualPackId() != null)
                                .collect(Collectors.groupingBy(RitualHistory::getRitualPackId,
                                                Collectors.mapping(RitualHistory::getRitualId, Collectors.toSet())));

                Set<UUID> ritualIds = activeRituals.stream()
                                .map(RitualHistory::getRitualId)
                                .collect(Collectors.toSet());

                Map<UUID, RitualDTO> ritualsMap = ritualService.findAllById(new ArrayList<>(ritualIds)).stream()
                                .collect(Collectors.toMap(RitualDTO::getId, Function.identity()));

                Map<UUID, RitualPackDTO> ritualPacksMap = ritualPackService.findAllById(new ArrayList<>(ritualPackIds))
                                .stream()
                                .collect(Collectors.toMap(RitualPackDTO::getId, Function.identity()));

                for (UUID packId : ritualPacksMap.keySet()) {
                        RitualPackDTO ritualPack = ritualPacksMap.get(packId);
                        List<RitualDTO> rituals = ritualPackToRituals.getOrDefault(packId, Collections.emptySet())
                                        .stream()
                                        .map(ritualsMap::get)
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toList());
                        ritualPack.setRituals(rituals);
                }

                Set<UUID> standaloneRitualIds = activeRituals.stream()
                                .filter(rh -> rh.getRitualPackId() == null)
                                .map(RitualHistory::getRitualId)
                                .collect(Collectors.toSet());

                List<RitualDTO> standaloneRituals = standaloneRitualIds.stream()
                                .map(ritualsMap::get)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());

                return CurrentRitualsDTO.builder()
                                .ritualHistoryMap(ritualHistoryMap)
                                .ritualPacks(new ArrayList<>(ritualPacksMap.values()))
                                .rituals(standaloneRituals)
                                .build();
        }

        public Optional<RitualHistoryDTO> findById(UUID id) {
                return ritualHistoryRepository.findById(id)
                                .map(RitualHistoryMapper::toDto);
        }

        @Transactional
        public RitualHistoryDTO create(UUID userId, RitualHistoryDTO entity) {
                RitualHistory ritualHistory = RitualHistoryMapper.fromDto(entity);
                ritualHistory.setUserId(userId);
                RitualHistory saved = ritualHistoryRepository.saveAndFlush(ritualHistory);
                return RitualHistoryMapper.toDto(saved);
        }

        @Transactional
        public void delete(UUID ritualHistoryId) {
                ritualHistoryRepository.deleteById(ritualHistoryId);
        }

        @Transactional
        public List<RitualHistoryDTO> bulkCreateRitualHistories(UUID userId, List<RitualHistoryDTO> ritualHistories) {
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
                                throw new EntityNotFoundException(
                                                "The following ritual pack IDs were not found: " + missingPacks);
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
                List<RitualHistory> savedHistories = ritualHistoryRepository.saveAllAndFlush(histories);

                // Convert back to DTOs and return
                return savedHistories.stream()
                                .map(RitualHistoryMapper::toDto)
                                .collect(Collectors.toList());
        }

        @Transactional
        public RitualHistoryDTO updateStatus(UUID ritualHistoryId, UUID userId, RitualHistoryStatus status,
                        EmojiFeedback feedback) {
                RitualHistory ritualHistory = ritualHistoryRepository.findById(ritualHistoryId)
                                .filter(history -> history.getUserId().equals(userId))
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Ritual history not found with id: " + ritualHistoryId));

                ritualHistory.setStatus(status);
                if (feedback != null) {
                        ritualHistory.setFeedback(feedback);
                }
                RitualHistory saved = ritualHistoryRepository.save(ritualHistory);
                return RitualHistoryMapper.toDto(saved);
        }

        @Transactional
        public List<RitualHistoryDTO> bulkUpdateStatus(UUID userId, List<StatusUpdateEntry> updates) {

                // Get all ritual history IDs from the updates
                List<UUID> ritualHistoryIds = updates.stream()
                                .map(StatusUpdateEntry::getRitualHistoryId)
                                .collect(Collectors.toList());

                // Find all ritual histories that belong to the user
                Map<UUID, RitualHistory> historyMap = ritualHistoryRepository.findAllById(ritualHistoryIds).stream()
                                .filter(rh -> rh.getUserId().equals(userId))
                                .collect(Collectors.toMap(RitualHistory::getId, rh -> rh));

                // Create a map of updates by ritual history ID for quick lookup
                Map<UUID, StatusUpdateEntry> updatesMap = updates.stream()
                                .collect(Collectors.toMap(
                                                StatusUpdateEntry::getRitualHistoryId,
                                                update -> update));

                // Update each history with its corresponding status and feedback
                List<RitualHistory> historiesToUpdate = new ArrayList<>();
                for (Map.Entry<UUID, RitualHistory> entry : historyMap.entrySet()) {
                        UUID historyId = entry.getKey();
                        RitualHistory history = entry.getValue();
                        StatusUpdateEntry update = updatesMap.get(historyId);

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
