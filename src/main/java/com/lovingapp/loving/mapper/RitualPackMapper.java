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
                .tagLine(pack.getTagLine())
                .description(pack.getDescription())
                .howItHelps(pack.getHowItHelps())
                .rituals((pack.getRituals() != null ? pack.getRituals() : Collections.<Ritual>emptyList())
                        .stream()
                        .map(ritual -> RitualMapper.toDto(ritual))
                        .collect(Collectors.toList()))
                // setting ritualIds for quick lookup
                .ritualIds((pack.getRituals() != null ? pack.getRituals() : Collections.<Ritual>emptyList())
                        .stream()
                        .map(Ritual::getId)
                        .collect(Collectors.toList()))
                .journey(pack.getJourney())
                .loveTypes(Objects.requireNonNullElse(pack.getLoveTypes(), Collections.emptyList()))
                .relationalNeeds(Objects.requireNonNullElse(pack.getRelationalNeeds(), Collections.emptyList()))
                .mediaAssets(Objects.requireNonNullElse(pack.getMediaAssets(), Collections.emptyList()))
                .semanticSummary(pack.getSemanticSummary())
                .status(pack.getStatus())
                .contentHash(pack.getContentHash())
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
        entity.setTagLine(dto.getTagLine());
        entity.setDescription(dto.getDescription());
        entity.setHowItHelps(dto.getHowItHelps());
        entity.setJourney(dto.getJourney());
        entity.setSemanticSummary(dto.getSemanticSummary());
        entity.setStatus(dto.getStatus());
        entity.setContentHash(dto.getContentHash());
        entity.setLoveTypes(Objects.requireNonNullElse(dto.getLoveTypes(), Collections.emptyList()));
        entity.setRelationalNeeds(Objects.requireNonNullElse(dto.getRelationalNeeds(), Collections.emptyList()));
        entity.setMediaAssets(Objects.requireNonNullElse(dto.getMediaAssets(), Collections.emptyList()));
        // rituals are set in the service layer by fetching using ritualIds
        return entity;
    }

    public static void updateEntityFromDto(RitualPackDTO dto, RitualPack entity) {
        if (dto == null || entity == null)
            return;
        entity.setTitle(dto.getTitle());
        entity.setTagLine(dto.getTagLine());
        entity.setDescription(dto.getDescription());
        entity.setHowItHelps(dto.getHowItHelps());
        entity.setJourney(dto.getJourney());
        entity.setSemanticSummary(dto.getSemanticSummary());
        entity.setStatus(dto.getStatus());
        entity.setContentHash(dto.getContentHash());
        entity.setLoveTypes(Objects.requireNonNullElse(dto.getLoveTypes(), Collections.emptyList()));
        entity.setRelationalNeeds(Objects.requireNonNullElse(dto.getRelationalNeeds(), Collections.emptyList()));
        entity.setMediaAssets(Objects.requireNonNullElse(dto.getMediaAssets(), Collections.emptyList()));
        // rituals are set in the service layer by fetching using ritualIds
    }
}
