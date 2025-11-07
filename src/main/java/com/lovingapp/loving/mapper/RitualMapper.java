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
                                .description(ritual.getDescription())
                                .ritualMode(ritual.getRitualMode())
                                .timeTaken(ritual.getTimeTaken())
                                .semanticSummary(ritual.getSemanticSummary())
                                .status(ritual.getStatus())
                                .contentHash(ritual.getContentHash())
                                .createdAt(ritual.getCreatedAt())
                                .updatedAt(ritual.getUpdatedAt())
                                .ritualTones(Objects.requireNonNullElse(ritual.getRitualTones(),
                                                Collections.emptyList()))
                                .steps(Objects.requireNonNullElse(ritual.getSteps(), Collections.emptyList()))
                                .mediaAssets(Objects.requireNonNullElse(ritual.getMediaAssets(),
                                                Collections.emptyList()))
                                .loveTypes(Objects.requireNonNullElse(ritual.getLoveTypes(),
                                                Collections.emptyList()))
                                .relationalNeeds(Objects.requireNonNullElse(ritual.getRelationalNeeds(),
                                                Collections.emptyList()))
                                .build();
        }

        public static Ritual fromDto(RitualDTO dto) {
                if (dto == null)
                        return null;
                Ritual entity = new Ritual();
                entity.setId(dto.getId());
                entity.setTitle(dto.getTitle());
                entity.setDescription(dto.getDescription());
                entity.setRitualMode(dto.getRitualMode());
                entity.setTimeTaken(dto.getTimeTaken());
                entity.setSemanticSummary(dto.getSemanticSummary());
                entity.setStatus(dto.getStatus());
                entity.setContentHash(dto.getContentHash());
                // Do not set createdAt/updatedAt; they are managed by JPA timestamps

                entity.setRitualTones(Objects.requireNonNullElse(dto.getRitualTones(), Collections.emptyList()));
                entity.setSteps(Objects.requireNonNullElse(dto.getSteps(), Collections.emptyList()));
                entity.setMediaAssets(Objects.requireNonNullElse(dto.getMediaAssets(), Collections.emptyList()));
                entity.setLoveTypes(Objects.requireNonNullElse(dto.getLoveTypes(), Collections.emptyList()));
                entity.setRelationalNeeds(
                                Objects.requireNonNullElse(dto.getRelationalNeeds(), Collections.emptyList()));
                return entity;
        }

        public static void updateEntityFromDto(RitualDTO dto, Ritual entity) {
                if (dto == null || entity == null)
                        return;
                entity.setTitle(dto.getTitle());
                entity.setDescription(dto.getDescription());
                entity.setRitualMode(dto.getRitualMode());
                entity.setTimeTaken(dto.getTimeTaken());
                entity.setSemanticSummary(dto.getSemanticSummary());
                entity.setStatus(dto.getStatus());
                entity.setContentHash(dto.getContentHash());

                entity.setRitualTones(Objects.requireNonNullElse(dto.getRitualTones(), Collections.emptyList()));
                entity.setSteps(Objects.requireNonNullElse(dto.getSteps(), Collections.emptyList()));
                entity.setMediaAssets(Objects.requireNonNullElse(dto.getMediaAssets(), Collections.emptyList()));
                entity.setLoveTypes(Objects.requireNonNullElse(dto.getLoveTypes(), Collections.emptyList()));
                entity.setRelationalNeeds(
                                Objects.requireNonNullElse(dto.getRelationalNeeds(), Collections.emptyList()));
        }
}
