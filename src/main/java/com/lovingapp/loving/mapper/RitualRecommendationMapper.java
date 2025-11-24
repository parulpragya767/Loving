package com.lovingapp.loving.mapper;

import com.lovingapp.loving.model.dto.RitualRecommendationDTOs.RitualRecommendationDTO;
import com.lovingapp.loving.model.entity.RitualRecommendation;

public final class RitualRecommendationMapper {
    private RitualRecommendationMapper() {
    }

    public static RitualRecommendationDTO toDto(RitualRecommendation entity) {
        if (entity == null)
            return null;
        return RitualRecommendationDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .source(entity.getSource())
                .sourceId(entity.getSourceId())
                .ritualPackId(entity.getRitualPackId())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static RitualRecommendation fromDto(RitualRecommendationDTO dto) {
        if (dto == null)
            return null;
        return RitualRecommendation.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .source(dto.getSource())
                .sourceId(dto.getSourceId())
                .ritualPackId(dto.getRitualPackId())
                .status(dto.getStatus())
                .build();
    }

    public static void updateFromDto(RitualRecommendationDTO dto, RitualRecommendation entity) {
        if (dto == null || entity == null)
            return;
        entity.setSource(dto.getSource());
        entity.setSourceId(dto.getSourceId());
        entity.setRitualPackId(dto.getRitualPackId());
        entity.setStatus(dto.getStatus());
    }
}
