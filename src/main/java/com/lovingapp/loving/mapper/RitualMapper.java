package com.lovingapp.loving.mapper;

import com.lovingapp.loving.dto.RitualDTO;
import com.lovingapp.loving.model.Ritual;

import java.util.Collections;
import java.util.Objects;

public final class RitualMapper {

    private RitualMapper() {}

    public static RitualDTO toDto(Ritual ritual) {
        if (ritual == null) return null;
        RitualDTO dto = new RitualDTO();
        // Scalars and simple types
        dto.setId(ritual.getId() != null ? ritual.getId().toString() : null);
        dto.setTitle(ritual.getTitle());
        dto.setShortDescription(ritual.getShortDescription());
        dto.setFullDescription(ritual.getFullDescription());
        dto.setRitualMode(ritual.getRitualMode());
        dto.setSensitivityLevel(ritual.getSensitivityLevel());
        dto.setEffortLevel(ritual.getEffortLevel());
        dto.setEstimatedDurationMinutes(ritual.getEstimatedDurationMinutes());
        dto.setRhythm(ritual.getRhythm());
        dto.setSemanticSummary(ritual.getSemanticSummary());
        dto.setStatus(ritual.getStatus());
        dto.setCreatedBy(ritual.getCreatedBy());
        dto.setCreatedAt(ritual.getCreatedAt());
        dto.setUpdatedAt(ritual.getUpdatedAt());

        // Collections with null-safety (prefer empty lists to null)
        dto.setRitualTypes(Objects.requireNonNullElse(ritual.getRitualTypes(), Collections.emptyList()));
        dto.setTones(Objects.requireNonNullElse(ritual.getTones(), Collections.emptyList()));
        dto.setRitualSteps(Objects.requireNonNullElse(ritual.getRitualSteps(), Collections.emptyList()));
        dto.setMediaAssets(Objects.requireNonNullElse(ritual.getMediaAssets(), Collections.emptyList()));
        dto.setLoveTypesSupported(Objects.requireNonNullElse(ritual.getLoveTypesSupported(), Collections.emptyList()));
        dto.setEmotionalStatesSupported(Objects.requireNonNullElse(ritual.getEmotionalStatesSupported(), Collections.emptyList()));
        dto.setRelationalNeedsServed(Objects.requireNonNullElse(ritual.getRelationalNeedsServed(), Collections.emptyList()));
        dto.setLifeContextsRelevant(Objects.requireNonNullElse(ritual.getLifeContextsRelevant(), Collections.emptyList()));
        dto.setPreparationRequirements(Objects.requireNonNullElse(ritual.getPreparationRequirements(), Collections.emptyList()));

        return dto;
    }
}
