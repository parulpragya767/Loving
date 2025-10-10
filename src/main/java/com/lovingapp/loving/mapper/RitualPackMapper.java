package com.lovingapp.loving.mapper;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

import com.lovingapp.loving.model.dto.RitualPackDTO;
import com.lovingapp.loving.model.entity.Ritual;
import com.lovingapp.loving.model.entity.RitualPack;

public final class RitualPackMapper {

    private RitualPackMapper() {
    }

    public static RitualPackDTO toDto(RitualPack pack) {
        if (pack == null)
            return null;
        return RitualPackDTO.builder()
                .id(pack.getId())
                .title(pack.getTitle())
                .shortDescription(pack.getShortDescription())
                .fullDescription(pack.getFullDescription())
                .ritualIds((pack.getRituals() != null ? pack.getRituals() : Collections.<Ritual>emptyList())
                        .stream().map(Ritual::getId).collect(Collectors.toList()))
                .ritualTypes(Objects.requireNonNullElse(pack.getRitualTypes(), Collections.emptyList()))
                .ritualTones(Objects.requireNonNullElse(pack.getRitualTones(), Collections.emptyList()))
                .sensitivityLevel(pack.getSensitivityLevel())
                .effortLevel(pack.getEffortLevel())
                .loveTypesSupported(Objects.requireNonNullElse(pack.getLoveTypesSupported(), Collections.emptyList()))
                .emotionalStatesSupported(
                        Objects.requireNonNullElse(pack.getEmotionalStatesSupported(), Collections.emptyList()))
                .relationalNeedsServed(
                        Objects.requireNonNullElse(pack.getRelationalNeedsServed(), Collections.emptyList()))
                .lifeContextsRelevant(
                        Objects.requireNonNullElse(pack.getLifeContextsRelevant(), Collections.emptyList()))
                .semanticSummary(pack.getSemanticSummary())
                .status(pack.getStatus())
                .createdBy(pack.getCreatedBy())
                .createdAt(pack.getCreatedAt())
                .updatedAt(pack.getUpdatedAt())
                .build();
    }

    public static RitualPack fromDto(RitualPackDTO dto) {
        if (dto == null)
            return null;
        RitualPack entity = new RitualPack();
        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());
        entity.setShortDescription(dto.getShortDescription());
        entity.setFullDescription(dto.getFullDescription());
        entity.setSensitivityLevel(dto.getSensitivityLevel());
        entity.setEffortLevel(dto.getEffortLevel());
        entity.setSemanticSummary(dto.getSemanticSummary());
        entity.setStatus(dto.getStatus());
        entity.setCreatedBy(dto.getCreatedBy());
        // rituals are set by service based on ritualIds
        return entity;
    }

    public static void updateEntityFromDto(RitualPackDTO dto, RitualPack entity) {
        if (dto == null || entity == null)
            return;
        entity.setTitle(dto.getTitle());
        entity.setShortDescription(dto.getShortDescription());
        entity.setFullDescription(dto.getFullDescription());
        entity.setSensitivityLevel(dto.getSensitivityLevel());
        entity.setEffortLevel(dto.getEffortLevel());
        entity.setSemanticSummary(dto.getSemanticSummary());
        entity.setStatus(dto.getStatus());
        entity.setCreatedBy(dto.getCreatedBy());
        // rituals are managed by service
    }
}
