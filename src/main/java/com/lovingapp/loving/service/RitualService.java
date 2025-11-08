package com.lovingapp.loving.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.loving.exception.ResourceNotFoundException;
import com.lovingapp.loving.mapper.RitualMapper;
import com.lovingapp.loving.model.dto.RitualDTO;
import com.lovingapp.loving.model.dto.RitualFilterRequest;
import com.lovingapp.loving.model.dto.RitualTagDTO;
import com.lovingapp.loving.model.dto.RitualTagsDTO;
import com.lovingapp.loving.model.dto.TagValueDTO;
import com.lovingapp.loving.model.entity.Ritual;
import com.lovingapp.loving.model.enums.LoveType;
import com.lovingapp.loving.model.enums.RelationalNeed;
import com.lovingapp.loving.model.enums.RitualMode;
import com.lovingapp.loving.repository.RitualRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RitualService {

    private final RitualRepository ritualRepository;

    @Transactional(readOnly = true)
    public List<RitualDTO> getAllRituals() {
        return ritualRepository.findAll().stream()
                .map(RitualMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RitualDTO getRitualById(UUID id) {
        return ritualRepository.findById(id)
                .map(RitualMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Ritual not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<RitualDTO> findAllById(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ritualRepository.findAllById(ids).stream()
                .map(RitualMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<RitualDTO> searchRituals(RitualFilterRequest filter, Pageable pageable) {
        Page<Ritual> result = ritualRepository.search(filter, pageable);
        return result.map(RitualMapper::toDto);
    }

    @Transactional(readOnly = true)
    public RitualTagsDTO getRitualTags() {
        RitualTagDTO loveTypes = new RitualTagDTO(
                "Love Types",
                1,
                toTagValues(LoveType.values()));

        RitualTagDTO ritualModes = new RitualTagDTO(
                "Ritual Modes",
                2,
                toTagValues(RitualMode.values()));

        RitualTagDTO relationalNeeds = new RitualTagDTO(
                "Relational Needs",
                3,
                toTagValues(RelationalNeed.values()));

        return new RitualTagsDTO(
                loveTypes,
                ritualModes,
                relationalNeeds);
    }

    private List<TagValueDTO> toTagValues(Enum<?>[] values) {
        Class<?> declaring = values.length > 0 ? values[0].getDeclaringClass() : Enum.class;
        boolean pretty = declaring == LoveType.class || declaring == RitualMode.class;
        return Arrays.stream(values)
                .map(e -> new TagValueDTO(e.name(), pretty ? humanize(e.name()) : e.toString()))
                .collect(Collectors.toList());
    }

    private String humanize(String name) {
        String[] parts = name.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty())
                continue;
            sb.append(Character.toUpperCase(parts[i].charAt(0)))
                    .append(parts[i].substring(1));
            if (i < parts.length - 1)
                sb.append(' ');
        }
        return sb.toString();
    }

    @Transactional
    public RitualDTO createRitual(RitualDTO ritualDTO) {
        Ritual ritual = RitualMapper.fromDto(ritualDTO);
        Ritual savedRitual = ritualRepository.save(ritual);
        return RitualMapper.toDto(savedRitual);
    }

    @Transactional
    public RitualDTO updateRitual(UUID id, RitualDTO ritualDTO) {
        return ritualRepository.findById(id)
                .map(existingRitual -> {
                    ritualDTO.setId(id); // Ensure ID consistency
                    RitualMapper.updateEntityFromDto(ritualDTO, existingRitual);
                    Ritual updatedRitual = ritualRepository.save(existingRitual);
                    return RitualMapper.toDto(updatedRitual);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Ritual not found with id: " + id));
    }

    @Transactional
    public void deleteRitual(UUID id) {
        Ritual ritual = ritualRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ritual not found with id: " + id));
        ritualRepository.delete(ritual);
    }

    @Transactional
    public List<RitualDTO> bulkCreate(List<RitualDTO> ritualDTOs) {
        if (ritualDTOs == null || ritualDTOs.isEmpty()) {
            return Collections.emptyList();
        }
        List<Ritual> entities = ritualDTOs.stream()
                .map(RitualMapper::fromDto)
                .collect(Collectors.toList());
        List<Ritual> saved = ritualRepository.saveAll(entities);
        return saved.stream().map(RitualMapper::toDto).collect(Collectors.toList());
    }

    @Transactional
    public List<RitualDTO> bulkUpdate(List<RitualDTO> ritualDTOs) {
        if (ritualDTOs == null || ritualDTOs.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> ids = ritualDTOs.stream()
                .map(RitualDTO::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        Map<UUID, Ritual> existingById = ritualRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Ritual::getId, Function.identity()));

        List<Ritual> toSave = ritualDTOs.stream()
                .map(dto -> {
                    Ritual existing = existingById.get(dto.getId());
                    if (existing == null) {
                        // If not found, treat as new entity to be saved
                        return RitualMapper.fromDto(dto);
                    }
                    RitualMapper.updateEntityFromDto(dto, existing);
                    return existing;
                })
                .collect(Collectors.toList());

        List<Ritual> saved = ritualRepository.saveAll(toSave);
        return saved.stream().map(RitualMapper::toDto).collect(Collectors.toList());
    }
}
