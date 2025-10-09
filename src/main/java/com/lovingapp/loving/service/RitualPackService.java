package com.lovingapp.loving.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.loving.dto.RitualPackDTO;
import com.lovingapp.loving.mapper.RitualPackMapper;
import com.lovingapp.loving.model.Ritual;
import com.lovingapp.loving.model.RitualPack;
import com.lovingapp.loving.repository.RitualPackRepository;
import com.lovingapp.loving.repository.RitualRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
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

    @Transactional
    public RitualPackDTO create(RitualPackDTO dto) {
        RitualPack entity = RitualPackMapper.fromDto(dto);
        List<Ritual> rituals = resolveRituals(dto.getRitualIds());
        entity.setRituals(rituals);
        aggregateTagsFromRituals(entity, rituals);
        RitualPack saved = ritualPackRepository.save(entity);
        return RitualPackMapper.toDto(saved);
    }

    @Transactional
    public Optional<RitualPackDTO> update(UUID id, RitualPackDTO dto) {
        return ritualPackRepository.findById(id).map(existing -> {
            RitualPackMapper.updateEntityFromDto(dto, existing);
            List<Ritual> rituals;
            if (dto.getRitualIds() != null) {
                rituals = resolveRituals(dto.getRitualIds());
                existing.setRituals(rituals);
            } else {
                rituals = existing.getRituals() != null
                        ? existing.getRituals()
                        : Collections.emptyList();
            }
            aggregateTagsFromRituals(existing, rituals);
            RitualPack saved = ritualPackRepository.save(existing);
            return RitualPackMapper.toDto(saved);
        });
    }

    @Transactional
    public boolean deleteById(UUID id) {
        if (ritualPackRepository.existsById(id)) {
            ritualPackRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private List<Ritual> resolveRituals(List<UUID> ritualIds) {
        if (ritualIds == null || ritualIds.isEmpty())
            return Collections.emptyList();
        return ritualRepository.findAllById(ritualIds);
    }

    private void aggregateTagsFromRituals(RitualPack pack, List<Ritual> rituals) {
        List<Ritual> safeRituals = Optional.ofNullable(rituals).orElse(Collections.emptyList());

        pack.setRitualTypes(unionDistinct(safeRituals, Ritual::getRitualTypes));
        pack.setRitualTones(unionDistinct(safeRituals, Ritual::getRitualTones));
        pack.setLoveTypesSupported(unionDistinct(safeRituals, Ritual::getLoveTypesSupported));
        pack.setEmotionalStatesSupported(unionDistinct(safeRituals, Ritual::getEmotionalStatesSupported));
        pack.setRelationalNeedsServed(unionDistinct(safeRituals, Ritual::getRelationalNeedsServed));
        pack.setLifeContextsRelevant(unionDistinct(safeRituals, Ritual::getLifeContextsRelevant));
    }

    private <T> List<T> unionDistinct(List<Ritual> rituals, Function<Ritual, List<T>> extractor) {
        return rituals.stream()
                .flatMap(r -> Optional.ofNullable(extractor.apply(r)).orElse(Collections.emptyList()).stream())
                .distinct()
                .collect(Collectors.toList());
    }
}
