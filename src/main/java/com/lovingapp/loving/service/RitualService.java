package com.lovingapp.loving.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
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

import com.lovingapp.loving.exception.BulkResourceAlreadyExistsException;
import com.lovingapp.loving.exception.ResourceAlreadyExistsException;
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
    public List<RitualDTO> findAll() {
        return ritualRepository.findAll().stream()
                .map(RitualMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RitualDTO findById(UUID id) {
        return ritualRepository.findById(id)
                .map(RitualMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Ritual", "id", id));
    }

    @Transactional(readOnly = true)
    public List<RitualDTO> findAllById(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<RitualDTO> rituals = ritualRepository.findAllById(ids).stream()
                .map(RitualMapper::toDto)
                .collect(Collectors.toList());

        List<UUID> existingRitualIds = rituals.stream()
                .map(RitualDTO::getId)
                .collect(Collectors.toList());

        Set<UUID> missingRitualIds = ids.stream()
                .filter(id -> !existingRitualIds.contains(id))
                .collect(Collectors.toSet());

        if (!missingRitualIds.isEmpty()) {
            throw new ResourceNotFoundException("Ritual", "ids", missingRitualIds);
        }
        return rituals;
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
        log.info("Creating ritual ritualId={}", ritualDTO.getId());

        if (ritualDTO.getId() != null && ritualRepository.existsById(ritualDTO.getId())) {
            throw new ResourceAlreadyExistsException("Ritual", "id", ritualDTO.getId());
        }

        Ritual ritual = RitualMapper.fromDto(ritualDTO);
        Ritual savedRitual = ritualRepository.save(ritual);

        log.info("Ritual created successfully ritualId={}", savedRitual.getId());
        return RitualMapper.toDto(savedRitual);
    }

    @Transactional
    public void updateRitual(UUID id, RitualDTO ritualDTO) {
        log.info("Updating ritual ritualId={}", id);

        Ritual ritual = ritualRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ritual", "id", id));

        RitualMapper.updateEntityFromDto(ritualDTO, ritual);
        ritualRepository.save(ritual);

        log.info("Ritual updated successfully ritualId={}", id);
    }

    @Transactional
    public void deleteRitual(UUID id) {
        log.info("Deleting ritual ritualId={}", id);

        Ritual ritual = ritualRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ritual", "id", id));
        ritualRepository.delete(ritual);

        log.info("Ritual deleted successfully ritualId={}", id);
    }

    @Transactional
    public List<RitualDTO> bulkCreate(List<RitualDTO> ritualDTOs) {
        log.info("Bulk creating rituals count={}", ritualDTOs == null ? 0 : ritualDTOs.size());

        if (ritualDTOs == null || ritualDTOs.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> ids = ritualDTOs.stream()
                .map(RitualDTO::getId)
                .toList();

        List<UUID> existingIds = ritualRepository.findByIdIn(ids)
                .stream()
                .map(Ritual::getId)
                .toList();

        if (!existingIds.isEmpty()) {
            throw new BulkResourceAlreadyExistsException("Ritual", existingIds.size());
        }

        List<Ritual> entities = ritualDTOs.stream()
                .map(RitualMapper::fromDto)
                .collect(Collectors.toList());

        List<Ritual> saved = ritualRepository.saveAll(entities);

        List<RitualDTO> result = saved.stream()
                .map(RitualMapper::toDto)
                .collect(Collectors.toList());

        log.info("Bulk rituals created successfully count={}", result.size());
        return result;
    }

    @Transactional
    public void bulkUpdate(List<RitualDTO> ritualDTOs) {
        log.info("Bulk update requested for rituals count={}", ritualDTOs.size());
        if (ritualDTOs == null || ritualDTOs.isEmpty()) {
            return;
        }

        List<UUID> ids = ritualDTOs.stream()
                .map(RitualDTO::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            return;
        }

        if (ids.size() != new HashSet<>(ids).size()) {
            throw new IllegalArgumentException("Duplicate ritual IDs in bulk update request");
        }

        Map<UUID, Ritual> existingById = ritualRepository.findByIdIn(ids).stream()
                .collect(Collectors.toMap(Ritual::getId, Function.identity()));

        if (existingById.size() != ids.size()) {
            int missingCount = ids.size() - existingById.size();
            throw new BulkResourceAlreadyExistsException("Ritual", missingCount);
        }

        log.info("Bulk update validated existingCount={}", existingById.size());

        List<Ritual> toSave = ritualDTOs.stream()
                .map(dto -> {
                    Ritual existing = existingById.get(dto.getId());
                    RitualMapper.updateEntityFromDto(dto, existing);
                    return existing;
                })
                .collect(Collectors.toList());

        ritualRepository.saveAll(toSave);

        log.info("Bulk rituals updated successfully.");
    }
}
