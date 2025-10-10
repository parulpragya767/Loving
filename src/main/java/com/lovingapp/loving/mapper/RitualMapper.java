package com.lovingapp.loving.mapper;

import java.util.Collections;
import java.util.Objects;

import com.lovingapp.loving.model.dto.RitualDTO;
import com.lovingapp.loving.model.entity.Ritual;

public final class RitualMapper {

        private RitualMapper() {
        }

        public static RitualDTO toDto(Ritual ritual) {
                if (ritual == null)
                        return null;
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
                                .ritualTypes(Objects.requireNonNullElse(ritual.getRitualTypes(),
                                                Collections.emptyList()))
                                .ritualTones(Objects.requireNonNullElse(ritual.getRitualTones(),
                                                Collections.emptyList()))
                                .ritualSteps(Objects.requireNonNullElse(ritual.getRitualSteps(),
                                                Collections.emptyList()))
                                .mediaAssets(Objects.requireNonNullElse(ritual.getMediaAssets(),
                                                Collections.emptyList()))
                                .loveTypesSupported(Objects.requireNonNullElse(ritual.getLoveTypesSupported(),
                                                Collections.emptyList()))
                                .emotionalStatesSupported(
                                                Objects.requireNonNullElse(ritual.getEmotionalStatesSupported(),
                                                                Collections.emptyList()))
                                .relationalNeedsServed(
                                                Objects.requireNonNullElse(ritual.getRelationalNeedsServed(),
                                                                Collections.emptyList()))
                                .lifeContextsRelevant(
                                                Objects.requireNonNullElse(ritual.getLifeContextsRelevant(),
                                                                Collections.emptyList()))
                                .preparationRequirements(
                                                Objects.requireNonNullElse(ritual.getPreparationRequirements(),
                                                                Collections.emptyList()))
                                .build();
        }

        public static Ritual fromDto(RitualDTO dto) {
                if (dto == null)
                        return null;
                Ritual entity = new Ritual();
                entity.setId(dto.getId());
                entity.setTitle(dto.getTitle());
                entity.setShortDescription(dto.getShortDescription());
                entity.setFullDescription(dto.getFullDescription());
                entity.setRitualMode(dto.getRitualMode());
                entity.setSensitivityLevel(dto.getSensitivityLevel());
                entity.setEffortLevel(dto.getEffortLevel());
                entity.setEstimatedDurationMinutes(dto.getEstimatedDurationMinutes());
                entity.setRhythm(dto.getRhythm());
                entity.setSemanticSummary(dto.getSemanticSummary());
                entity.setStatus(dto.getStatus());
                entity.setCreatedBy(dto.getCreatedBy());
                // Do not set createdAt/updatedAt; they are managed by JPA timestamps

                entity.setRitualTypes(Objects.requireNonNullElse(dto.getRitualTypes(), Collections.emptyList()));
                entity.setRitualTones(Objects.requireNonNullElse(dto.getRitualTones(), Collections.emptyList()));
                entity.setRitualSteps(Objects.requireNonNullElse(dto.getRitualSteps(), Collections.emptyList()));
                entity.setMediaAssets(Objects.requireNonNullElse(dto.getMediaAssets(), Collections.emptyList()));
                entity.setLoveTypesSupported(
                                Objects.requireNonNullElse(dto.getLoveTypesSupported(), Collections.emptyList()));
                entity.setEmotionalStatesSupported(
                                Objects.requireNonNullElse(dto.getEmotionalStatesSupported(), Collections.emptyList()));
                entity.setRelationalNeedsServed(
                                Objects.requireNonNullElse(dto.getRelationalNeedsServed(), Collections.emptyList()));
                entity.setLifeContextsRelevant(
                                Objects.requireNonNullElse(dto.getLifeContextsRelevant(), Collections.emptyList()));
                entity.setPreparationRequirements(
                                Objects.requireNonNullElse(dto.getPreparationRequirements(), Collections.emptyList()));
                return entity;
        }

        public static void updateEntityFromDto(RitualDTO dto, Ritual entity) {
                if (dto == null || entity == null)
                        return;
                entity.setTitle(dto.getTitle());
                entity.setShortDescription(dto.getShortDescription());
                entity.setFullDescription(dto.getFullDescription());
                entity.setRitualMode(dto.getRitualMode());
                entity.setSensitivityLevel(dto.getSensitivityLevel());
                entity.setEffortLevel(dto.getEffortLevel());
                entity.setEstimatedDurationMinutes(dto.getEstimatedDurationMinutes());
                entity.setRhythm(dto.getRhythm());
                entity.setSemanticSummary(dto.getSemanticSummary());
                entity.setStatus(dto.getStatus());
                entity.setCreatedBy(dto.getCreatedBy());

                entity.setRitualTypes(Objects.requireNonNullElse(dto.getRitualTypes(), Collections.emptyList()));
                entity.setRitualTones(Objects.requireNonNullElse(dto.getRitualTones(), Collections.emptyList()));
                entity.setRitualSteps(Objects.requireNonNullElse(dto.getRitualSteps(), Collections.emptyList()));
                entity.setMediaAssets(Objects.requireNonNullElse(dto.getMediaAssets(), Collections.emptyList()));
                entity.setLoveTypesSupported(
                                Objects.requireNonNullElse(dto.getLoveTypesSupported(), Collections.emptyList()));
                entity.setEmotionalStatesSupported(
                                Objects.requireNonNullElse(dto.getEmotionalStatesSupported(), Collections.emptyList()));
                entity.setRelationalNeedsServed(
                                Objects.requireNonNullElse(dto.getRelationalNeedsServed(), Collections.emptyList()));
                entity.setLifeContextsRelevant(
                                Objects.requireNonNullElse(dto.getLifeContextsRelevant(), Collections.emptyList()));
                entity.setPreparationRequirements(
                                Objects.requireNonNullElse(dto.getPreparationRequirements(), Collections.emptyList()));
        }
}
