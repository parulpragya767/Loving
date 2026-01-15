package com.lovingapp.loving.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.loving.exception.BulkResourceAlreadyExistsException;
import com.lovingapp.loving.exception.ResourceAlreadyExistsException;
import com.lovingapp.loving.exception.ResourceNotFoundException;
import com.lovingapp.loving.mapper.RitualPackMapper;
import com.lovingapp.loving.model.dto.RitualPackDTO;
import com.lovingapp.loving.model.entity.Ritual;
import com.lovingapp.loving.model.entity.RitualPack;
import com.lovingapp.loving.repository.RitualPackRepository;
import com.lovingapp.loving.repository.RitualRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RitualPackService {

    private final RitualPackRepository ritualPackRepository;
    private final RitualRepository ritualRepository;

    @Transactional(readOnly = true)
    public List<RitualPackDTO> findAll() {
        return ritualPackRepository.findAll().stream()
                .map(RitualPackMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RitualPackDTO findById(UUID id) {
        return ritualPackRepository.findById(id)
                .map(RitualPackMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("RitualPack", "id", id));
    }

    @Transactional(readOnly = true)
    public List<RitualPackDTO> findAllById(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<RitualPackDTO> ritualPacks = ritualPackRepository.findAllById(ids).stream()
                .map(RitualPackMapper::toDto)
                .collect(Collectors.toList());

        List<UUID> existingRitualPackIds = ritualPacks.stream()
                .map(RitualPackDTO::getId)
                .collect(Collectors.toList());

        Set<UUID> missingRitualPackIds = ids.stream()
                .filter(id -> !existingRitualPackIds.contains(id))
                .collect(Collectors.toSet());

        if (!missingRitualPackIds.isEmpty()) {
            throw new ResourceNotFoundException("RitualPack", "ids", missingRitualPackIds);
        }
        return ritualPacks;
    }

    @Transactional
    public RitualPackDTO create(RitualPackDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("RitualPackDTO cannot be null");
        }

        log.info("Creating ritual pack ritualPackId={}", dto.getId());

        if (dto.getId() != null && ritualPackRepository.existsById(dto.getId())) {
            throw new ResourceAlreadyExistsException("RitualPack", "id", dto.getId());
        }

        RitualPack entity = RitualPackMapper.fromDto(dto);
        if (dto.getRitualIds() != null) {
            List<Ritual> rituals = resolveRituals(dto.getRitualIds());
            entity.setRituals(rituals);
        }
        RitualPack saved = ritualPackRepository.save(entity);

        log.info("Ritual pack created successfully ritualPackId={}", saved.getId());
        return RitualPackMapper.toDto(saved);
    }

    @Transactional
    public void update(UUID id, RitualPackDTO dto) {
        log.info("Updating ritual pack ritualPackId={}", id);
        if (dto == null) {
            throw new IllegalArgumentException("RitualPackDTO cannot be null");
        }

        RitualPack existing = ritualPackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RitualPack", "id", id));

        RitualPackMapper.updateEntityFromDto(dto, existing);
        if (dto.getRitualIds() != null) {
            List<Ritual> rituals = resolveRituals(dto.getRitualIds());
            existing.setRituals(rituals);
        }

        RitualPack saved = ritualPackRepository.save(existing);

        log.info("Ritual pack updated successfully ritualPackId={}", saved.getId());
    }

    @Transactional
    public List<RitualPackDTO> bulkCreate(List<RitualPackDTO> dtos) {
        log.info("Bulk creating ritual packs count={}", dtos == null ? 0 : dtos.size());
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> ids = dtos.stream()
                .map(RitualPackDTO::getId)
                .filter(Objects::nonNull)
                .toList();

        List<UUID> existingIds = ritualPackRepository.findAllById(ids).stream()
                .map(RitualPack::getId)
                .toList();

        if (!existingIds.isEmpty()) {
            throw new BulkResourceAlreadyExistsException("RitualPack", existingIds.size());
        }

        // Resolve all rituals in one batch
        Map<UUID, List<Ritual>> ritualsByPackId = resolveRitualsAcrossPacks(dtos);

        // Create entities and set rituals from the map
        List<RitualPack> entities = dtos.stream()
                .map(dto -> {
                    RitualPack entity = RitualPackMapper.fromDto(dto);
                    List<Ritual> rituals = ritualsByPackId.get(dto.getId());
                    entity.setRituals(rituals);
                    return entity;
                })
                .collect(Collectors.toList());

        List<RitualPack> savedEntities = ritualPackRepository.saveAll(entities);
        List<RitualPackDTO> result = savedEntities.stream()
                .map(RitualPackMapper::toDto)
                .collect(Collectors.toList());

        log.info("Bulk ritual packs created successfully count={}", result.size());
        return result;
    }

    @Transactional
    public void bulkUpdate(List<RitualPackDTO> dtos) {
        log.info("Bulk updating ritual packs count={}", dtos == null ? 0 : dtos.size());
        if (dtos == null || dtos.isEmpty()) {
            return;
        }

        // Get all pack IDs for batch fetch
        List<UUID> packIds = dtos.stream()
                .map(RitualPackDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (packIds.isEmpty()) {
            return;
        }

        if (packIds.size() != new HashSet<>(packIds).size()) {
            throw new IllegalArgumentException("Duplicate ritual pack IDs in bulk update request");
        }

        // Fetch all existing entities in one query
        Map<UUID, RitualPack> existingPacks = ritualPackRepository.findAllById(packIds).stream()
                .collect(Collectors.toMap(RitualPack::getId, Function.identity()));

        if (existingPacks.size() != packIds.size()) {
            int missingCount = packIds.size() - existingPacks.size();
            throw new ResourceNotFoundException("RitualPack", "count", missingCount);
        }

        log.info("Bulk update validated existingCount={}", existingPacks.size());

        // Resolve all rituals in one batch
        Map<UUID, List<Ritual>> ritualsByPackId = resolveRitualsAcrossPacks(dtos);

        // Update entities and set rituals from the map
        List<RitualPack> toUpdate = new ArrayList<>();
        for (RitualPackDTO dto : dtos) {
            if (dto.getId() != null) {
                RitualPack existing = existingPacks.get(dto.getId());
                RitualPackMapper.updateEntityFromDto(dto, existing);
                if (dto.getRitualIds() != null) {
                    List<Ritual> rituals = ritualsByPackId.get(dto.getId());
                    existing.setRituals(rituals);
                }
                toUpdate.add(existing);
            }
        }

        ritualPackRepository.saveAll(toUpdate);
        log.info("Bulk ritual packs updated successfully.");
    }

    @Transactional
    public void deleteById(UUID id) {
        log.info("Deleting ritual pack ritualPackId={}", id);

        RitualPack ritualPack = ritualPackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RitualPack", "id", id));
        ritualPackRepository.delete(ritualPack);

        log.info("Ritual pack deleted successfully ritualPackId={}", id);
    }

    private List<Ritual> resolveRituals(List<UUID> ritualIds) {
        if (ritualIds == null || ritualIds.isEmpty())
            return Collections.emptyList();

        List<Ritual> rituals = ritualRepository.findAllById(ritualIds);
        List<UUID> existingRitualIds = rituals.stream()
                .map(Ritual::getId).toList();

        Set<UUID> missingRitualIds = ritualIds.stream()
                .filter(id -> !existingRitualIds.contains(id))
                .collect(Collectors.toSet());

        if (!missingRitualIds.isEmpty()) {
            throw new ResourceNotFoundException("Ritual", "ids", missingRitualIds);
        }

        return rituals;
    }

    private Map<UUID, List<Ritual>> resolveRitualsAcrossPacks(List<RitualPackDTO> dtos) {
        if (dtos == null || dtos.isEmpty())
            return Collections.emptyMap();

        // Collect all unique ritual IDs
        List<UUID> allRitualIds = dtos.stream()
                .filter(dto -> dto.getRitualIds() != null)
                .flatMap(dto -> dto.getRitualIds().stream())
                .distinct()
                .collect(Collectors.toList());

        // Resolve all rituals in one batch
        Map<UUID, Ritual> ritualMap = !allRitualIds.isEmpty()
                ? resolveRituals(allRitualIds).stream()
                        .collect(Collectors.toMap(Ritual::getId, Function.identity()))
                : Collections.emptyMap();

        Map<UUID, List<Ritual>> ritualsByPackId = dtos.stream()
                .collect(Collectors.toMap(RitualPackDTO::getId, dto -> {
                    if (dto.getRitualIds() != null && !dto.getRitualIds().isEmpty()) {
                        List<Ritual> rituals = dto.getRitualIds().stream()
                                .map(ritualMap::get)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                        return rituals;
                    }
                    return Collections.emptyList();
                }));
        return ritualsByPackId;
    }
}
