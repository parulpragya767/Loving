package com.lovingapp.loving.service;

import com.lovingapp.loving.dto.RitualPackDTO;
import com.lovingapp.loving.mapper.RitualPackMapper;
import com.lovingapp.loving.model.Ritual;
import com.lovingapp.loving.model.RitualPack;
import com.lovingapp.loving.repository.RitualPackRepository;
import com.lovingapp.loving.repository.RitualRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RitualPackServiceImpl implements RitualPackService {

    private final RitualPackRepository ritualPackRepository;
    private final RitualRepository ritualRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RitualPackDTO> findAll() {
        return ritualPackRepository.findAll().stream()
                .map(RitualPackMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RitualPackDTO> findById(UUID id) {
        return ritualPackRepository.findById(id).map(RitualPackMapper::toDto);
    }

    @Override
    @Transactional
    public RitualPackDTO create(RitualPackDTO dto) {
        RitualPack entity = RitualPackMapper.fromDto(dto);
        // Resolve rituals
        List<Ritual> rituals = resolveRituals(dto.getRitualIds());
        entity.setRituals(rituals);
        // Aggregate tags
        aggregateTags(entity);
        RitualPack saved = ritualPackRepository.save(entity);
        return RitualPackMapper.toDto(saved);
    }

    @Override
    @Transactional
    public Optional<RitualPackDTO> update(UUID id, RitualPackDTO dto) {
        return ritualPackRepository.findById(id).map(existing -> {
            RitualPackMapper.updateEntityFromDto(dto, existing);
            if (dto.getRitualIds() != null) {
                existing.setRituals(resolveRituals(dto.getRitualIds()));
            }
            aggregateTags(existing);
            RitualPack saved = ritualPackRepository.save(existing);
            return RitualPackMapper.toDto(saved);
        });
    }

    @Override
    @Transactional
    public boolean deleteById(UUID id) {
        if (ritualPackRepository.existsById(id)) {
            ritualPackRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private List<Ritual> resolveRituals(List<UUID> ritualIds) {
        if (ritualIds == null || ritualIds.isEmpty()) return Collections.emptyList();
        return ritualRepository.findAllById(ritualIds);
    }

    private void aggregateTags(RitualPack pack) {
        List<Ritual> rituals = Optional.ofNullable(pack.getRituals()).orElse(Collections.emptyList());

        // Union lists
        pack.setRitualTypes(rituals.stream()
                .flatMap(r -> Optional.ofNullable(r.getRitualTypes()).orElse(Collections.emptyList()).stream())
                .distinct()
                .collect(Collectors.toList()));

        pack.setRitualTones(rituals.stream()
                .flatMap(r -> Optional.ofNullable(r.getRitualTones()).orElse(Collections.emptyList()).stream())
                .distinct()
                .collect(Collectors.toList()));

        pack.setLoveTypesSupported(rituals.stream()
                .flatMap(r -> Optional.ofNullable(r.getLoveTypesSupported()).orElse(Collections.emptyList()).stream())
                .distinct()
                .collect(Collectors.toList()));

        pack.setEmotionalStatesSupported(rituals.stream()
                .flatMap(r -> Optional.ofNullable(r.getEmotionalStatesSupported()).orElse(Collections.emptyList()).stream())
                .distinct()
                .collect(Collectors.toList()));

        pack.setRelationalNeedsServed(rituals.stream()
                .flatMap(r -> Optional.ofNullable(r.getRelationalNeedsServed()).orElse(Collections.emptyList()).stream())
                .distinct()
                .collect(Collectors.toList()));

        pack.setLifeContextsRelevant(rituals.stream()
                .flatMap(r -> Optional.ofNullable(r.getLifeContextsRelevant()).orElse(Collections.emptyList()).stream())
                .distinct()
                .collect(Collectors.toList()));

        // Derive representative single-value enums by mode (most common) if present
        pack.setSensitivityLevel(modeEnum(rituals.stream()
                .map(Ritual::getSensitivityLevel)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())));

        pack.setEffortLevel(modeEnum(rituals.stream()
                .map(Ritual::getEffortLevel)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())));
    }

    private static <E extends Enum<E>> E modeEnum(List<E> values) {
        if (values == null || values.isEmpty()) return null;
        return values.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
