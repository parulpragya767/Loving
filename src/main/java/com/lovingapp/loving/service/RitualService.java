package com.lovingapp.loving.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.lovingapp.loving.model.dto.RitualFilterDTO;
import com.lovingapp.loving.model.dto.RitualTagDTOs.RitualTag;
import com.lovingapp.loving.model.dto.RitualTagDTOs.RitualTags;
import com.lovingapp.loving.model.dto.RitualTagDTOs.TagValue;
import com.lovingapp.loving.model.entity.Ritual;
import com.lovingapp.loving.model.enums.LoveType;
import com.lovingapp.loving.model.enums.RelationalNeed;
import com.lovingapp.loving.model.enums.RitualMode;
import com.lovingapp.loving.model.enums.RitualTone;
import com.lovingapp.loving.model.enums.TimeTaken;
import com.lovingapp.loving.repository.RitualRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RitualService {

    private final RitualRepository ritualRepository;

    @Transactional(readOnly = true)
    public List<RitualDTO> getAllRituals() {
        return ritualRepository.findAll().stream()
                .map(RitualMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return ritualRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public boolean validateRitualIds(List<UUID> ids) {
        List<UUID> existingRitualIds = findAllById(new ArrayList<>(ids))
                .stream()
                .map(RitualDTO::getId)
                .collect(Collectors.toList());

        Set<UUID> missingRitualIds = ids.stream()
                .filter(id -> !existingRitualIds.contains(id))
                .collect(Collectors.toSet());

        if (!missingRitualIds.isEmpty()) {
            throw new ResourceNotFoundException("Ritual", "ids", missingRitualIds);
        }
        return true;
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
    public Page<RitualDTO> searchRituals(RitualFilterDTO filter, Pageable pageable) {
        Page<Ritual> result = ritualRepository.search(filter, pageable);
        return result.map(RitualMapper::toDto);
    }

    @Transactional(readOnly = true)
    public RitualTags getRitualTags() {
        RitualTag loveTypes = new RitualTag(
                "Love Types",
                toTagValues(LoveType.values(), LoveType::getDisplayName));

        RitualTag ritualModes = new RitualTag(
                "Modes",
                toTagValues(RitualMode.values(), RitualMode::getDisplayName));

        RitualTag timeTaken = new RitualTag(
                "Time Taken",
                toTagValues(TimeTaken.values(), TimeTaken::getDisplayName));

        RitualTag relationalNeeds = new RitualTag(
                "Relationship Needs",
                toTagValues(RelationalNeed.values(), RelationalNeed::getDisplayName));

        RitualTag ritualTones = new RitualTag(
                "Ritual Tones",
                toTagValues(RitualTone.values(), RitualTone::getDisplayName));

        return new RitualTags(
                loveTypes,
                ritualModes,
                timeTaken,
                relationalNeeds,
                ritualTones);
    }

    private <E extends Enum<E>> List<TagValue> toTagValues(E[] values, Function<E, String> displayNameFn) {
        return Arrays.stream(values)
                .map(e -> new TagValue(e.name(), displayNameFn.apply(e)))
                .collect(Collectors.toList());
    }

    @Transactional
    public RitualDTO createRitual(RitualDTO ritualDTO) {
        log.info("Creating ritual");
        log.debug("Create ritual payload: {}", ritualDTO);
        Ritual ritual = RitualMapper.fromDto(ritualDTO);
        Ritual savedRitual = ritualRepository.save(ritual);
        log.info("Ritual created successfully ritualId={}", savedRitual.getId());
        return RitualMapper.toDto(savedRitual);
    }

    @Transactional
    public RitualDTO updateRitual(UUID id, RitualDTO ritualDTO) {
        log.info("Updating ritual ritualId={}", id);
        log.debug("Update ritual payload ritualId={} payload={}", id, ritualDTO);
        return ritualRepository.findById(id)
                .map(existingRitual -> {
                    ritualDTO.setId(id); // Ensure ID consistency
                    RitualMapper.updateEntityFromDto(ritualDTO, existingRitual);
                    Ritual updatedRitual = ritualRepository.save(existingRitual);
                    log.info("Ritual updated successfully ritualId={}", updatedRitual.getId());
                    return RitualMapper.toDto(updatedRitual);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Ritual not found with id: " + id));
    }

    @Transactional
    public void deleteRitual(UUID id) {
        log.info("Deleting ritual ritualId={}", id);
        Ritual ritual = ritualRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ritual not found with id: " + id));
        ritualRepository.delete(ritual);
        log.info("Ritual deleted successfully ritualId={}", id);
    }

    @Transactional
    public List<RitualDTO> bulkCreate(List<RitualDTO> ritualDTOs) {
        log.info("Bulk creating rituals count={}", ritualDTOs == null ? 0 : ritualDTOs.size());
        log.debug("Bulk create rituals payload: {}", ritualDTOs);
        if (ritualDTOs == null || ritualDTOs.isEmpty()) {
            return Collections.emptyList();
        }
        List<Ritual> entities = ritualDTOs.stream()
                .map(RitualMapper::fromDto)
                .collect(Collectors.toList());
        List<Ritual> saved = ritualRepository.saveAll(entities);
        List<RitualDTO> result = saved.stream().map(RitualMapper::toDto).collect(Collectors.toList());
        log.info("Bulk rituals created successfully count={}", result.size());
        return result;
    }

    @Transactional
    public List<RitualDTO> bulkUpdate(List<RitualDTO> ritualDTOs) {
        log.info("Bulk updating rituals count={}", ritualDTOs == null ? 0 : ritualDTOs.size());
        log.debug("Bulk update rituals payload: {}", ritualDTOs);
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
        List<RitualDTO> result = saved.stream().map(RitualMapper::toDto).collect(Collectors.toList());
        log.info("Bulk rituals updated successfully count={}", result.size());
        return result;
    }
}
