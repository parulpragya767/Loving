package com.lovingapp.loving.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Optional<RitualPackDTO> findById(UUID id) {
        return ritualPackRepository.findById(id).map(RitualPackMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<RitualPackDTO> findAllById(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ritualPackRepository.findAllById(ids).stream()
                .map(RitualPackMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public RitualPackDTO create(RitualPackDTO dto) {
        log.info("Creating ritual pack");
        log.debug("Create ritual pack payload: {}", dto);
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
    public Optional<RitualPackDTO> update(UUID id, RitualPackDTO dto) {
        log.info("Updating ritual pack ritualPackId={}", id);
        log.debug("Update ritual pack payload ritualPackId={} payload={}", id, dto);
        return ritualPackRepository.findById(id).map(existing -> {
            RitualPackMapper.updateEntityFromDto(dto, existing);
            if (dto.getRitualIds() != null) {
                List<Ritual> rituals = resolveRituals(dto.getRitualIds());
                existing.setRituals(rituals);
            }
            RitualPack saved = ritualPackRepository.save(existing);
            log.info("Ritual pack updated successfully ritualPackId={}", saved.getId());
            return RitualPackMapper.toDto(saved);
        });
    }

    @Transactional
    public List<RitualPackDTO> bulkCreate(List<RitualPackDTO> dtos) {
        log.info("Bulk creating ritual packs count={}", dtos == null ? 0 : dtos.size());
        log.debug("Bulk create ritual packs payload: {}", dtos);
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
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
    public List<RitualPackDTO> bulkUpdate(List<RitualPackDTO> dtos) {
        log.info("Bulk updating ritual packs count={}", dtos == null ? 0 : dtos.size());
        log.debug("Bulk update ritual packs payload: {}", dtos);
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }

        // Get all pack IDs for batch fetch
        List<UUID> packIds = dtos.stream()
                .map(RitualPackDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (packIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Fetch all existing entities in one query
        Map<UUID, RitualPack> existingPacks = ritualPackRepository.findAllById(packIds).stream()
                .collect(Collectors.toMap(RitualPack::getId, Function.identity()));

        // Resolve all rituals in one batch
        Map<UUID, List<Ritual>> ritualsByPackId = resolveRitualsAcrossPacks(dtos);

        // Update entities and set rituals from the map
        List<RitualPack> toUpdate = new ArrayList<>();
        for (RitualPackDTO dto : dtos) {
            if (dto.getId() != null) {
                RitualPack existing = existingPacks.get(dto.getId());
                if (existing != null) {
                    RitualPackMapper.updateEntityFromDto(dto, existing);
                    if (dto.getRitualIds() != null) {
                        List<Ritual> rituals = ritualsByPackId.get(dto.getId());
                        existing.setRituals(rituals);
                    }
                    toUpdate.add(existing);
                }
            }
        }

        List<RitualPack> updated = ritualPackRepository.saveAll(toUpdate);
        List<RitualPackDTO> result = updated.stream()
                .map(RitualPackMapper::toDto)
                .collect(Collectors.toList());
        log.info("Bulk ritual packs updated successfully count={}", result.size());
        return result;
    }

    @Transactional
    public boolean deleteById(UUID id) {
        log.info("Deleting ritual pack ritualPackId={}", id);
        if (ritualPackRepository.existsById(id)) {
            ritualPackRepository.deleteById(id);
            log.info("Ritual pack deleted successfully ritualPackId={}", id);
            return true;
        }
        log.info("Ritual pack delete skipped: not found ritualPackId={}", id);
        return false;
    }

    private List<Ritual> resolveRituals(List<UUID> ritualIds) {
        if (ritualIds == null || ritualIds.isEmpty())
            return Collections.emptyList();
        return ritualRepository.findAllById(ritualIds);
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
