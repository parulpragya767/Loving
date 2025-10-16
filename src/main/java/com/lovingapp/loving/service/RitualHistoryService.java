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
import com.lovingapp.loving.model.dto.CurrentRitualsDTO;
import com.lovingapp.loving.model.dto.RitualDTO;
import com.lovingapp.loving.model.dto.RitualHistoryDTO;
import com.lovingapp.loving.model.dto.RitualPackDTO;
import com.lovingapp.loving.model.entity.RitualHistory;
import com.lovingapp.loving.model.enums.EmojiFeedback;
import com.lovingapp.loving.model.enums.RitualHistoryStatus;
import com.lovingapp.loving.repository.RitualHistoryRepository;

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

    public CurrentRitualsDTO listActiveByUser(UUID userId) {
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
    public RitualHistoryDTO save(RitualHistoryDTO entity) {
        RitualHistory ritualHistory = RitualHistoryMapper.fromDto(entity);
        RitualHistory saved = ritualHistoryRepository.save(ritualHistory);
        return RitualHistoryMapper.toDto(saved);
    }

    @Transactional
    public void delete(RitualHistoryDTO entity) {
        RitualHistory ritualHistory = RitualHistoryMapper.fromDto(entity);
        ritualHistoryRepository.delete(ritualHistory);
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
}
