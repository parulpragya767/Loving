package com.lovingapp.mapper;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

import com.lovingapp.model.dto.RitualInPackDTO;
import com.lovingapp.model.dto.RitualPackDTO;
import com.lovingapp.model.entity.RitualPack;
import com.lovingapp.model.entity.RitualPackRitual;

public final class RitualPackMapper {

    private RitualPackMapper() {
    }

    public static RitualPackDTO toSummaryDto(RitualPack pack) {
        if (pack == null)
            return null;
        return RitualPackDTO.builder()
                .id(pack.getId())
                .title(pack.getTitle())
                .tagLine(pack.getTagLine())
                .shortDescription(pack.getShortDescription())
                .description(pack.getDescription())
                .howItHelps(pack.getHowItHelps())
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

    public static RitualPackDTO toDto(RitualPack pack) {
        if (pack == null)
            return null;

        var ritualPackRituals = pack.getRitualPackRituals() != null
                ? pack.getRitualPackRituals()
                : Collections.<RitualPackRitual>emptyList();

        var ritualsBuilder = RitualPackDTO.builder()
                .id(pack.getId())
                .title(pack.getTitle())
                .tagLine(pack.getTagLine())
                .shortDescription(pack.getShortDescription())
                .description(pack.getDescription())
                .howItHelps(pack.getHowItHelps());

        if (!ritualPackRituals.isEmpty()) {
            var ritualsList = ritualPackRituals.stream()
                    .map(rpr -> RitualInPackDTO.builder()
                            .ritual(RitualMapper.toDto(rpr.getRitual()))
                            .position(rpr.getPosition())
                            .build())
                    .collect(Collectors.toList());

            var ritualIdsList = ritualsList.stream()
                    .map(rip -> rip.getRitual().getId())
                    .collect(Collectors.toList());

            ritualsBuilder.rituals(ritualsList)
                    .ritualIds(ritualIdsList);
        } else {
            ritualsBuilder.rituals(Collections.emptyList())
                    .ritualIds(Collections.emptyList());
        }

        return ritualsBuilder
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
        entity.setShortDescription(dto.getShortDescription());
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
        entity.setShortDescription(dto.getShortDescription());
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
