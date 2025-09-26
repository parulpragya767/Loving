package com.lovingapp.loving.mapper;

import com.lovingapp.loving.dto.RitualDTO;
import com.lovingapp.loving.model.Ritual;

import java.util.Collections;
import java.util.Objects;

public final class RitualMapper {

    private RitualMapper() {}

    public static RitualDTO toDto(Ritual ritual) {
        if (ritual == null) return null;
        return RitualDTO.builder()
                .id(ritual.getId())
                .title(ritual.getTitle())
                .shortDescription(ritual.getShortDescription())
                .fullDescription(ritual.getFullDescription())
                .ritualMode(ritual.getRitualMode())
                .sensitivityLevel(ritual.getSensitivityLevel())
                .effortLevel(ritual.getEffortLevel())
                .estimatedDurationMinutes(ritual.getEstimatedDurationMinutes())
                .rhythm(ritual.getRhythm())
                .semanticSummary(ritual.getSemanticSummary())
                .status(ritual.getStatus())
                .createdBy(ritual.getCreatedBy())
                .createdAt(ritual.getCreatedAt())
                .updatedAt(ritual.getUpdatedAt())
                .ritualTypes(Objects.requireNonNullElse(ritual.getRitualTypes(), Collections.emptyList()))
                .ritualTones(Objects.requireNonNullElse(ritual.getRitualTones(), Collections.emptyList()))
                .ritualSteps(Objects.requireNonNullElse(ritual.getRitualSteps(), Collections.emptyList()))
                .mediaAssets(Objects.requireNonNullElse(ritual.getMediaAssets(), Collections.emptyList()))
                .loveTypesSupported(Objects.requireNonNullElse(ritual.getLoveTypesSupported(), Collections.emptyList()))
                .emotionalStatesSupported(Objects.requireNonNullElse(ritual.getEmotionalStatesSupported(), Collections.emptyList()))
                .relationalNeedsServed(Objects.requireNonNullElse(ritual.getRelationalNeedsServed(), Collections.emptyList()))
                .lifeContextsRelevant(Objects.requireNonNullElse(ritual.getLifeContextsRelevant(), Collections.emptyList()))
                .preparationRequirements(Objects.requireNonNullElse(ritual.getPreparationRequirements(), Collections.emptyList()))
                .build();
    }
}