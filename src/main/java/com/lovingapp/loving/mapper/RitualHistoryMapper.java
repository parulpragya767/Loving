package com.lovingapp.loving.mapper;

import com.lovingapp.loving.model.dto.RitualHistoryDTOs.RitualHistoryDTO;
import com.lovingapp.loving.model.entity.RitualHistory;

public final class RitualHistoryMapper {
    private RitualHistoryMapper() {
    }

    public static RitualHistoryDTO toDto(RitualHistory entity) {
        if (entity == null)
            return null;
        return RitualHistoryDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .ritualId(entity.getRitualId())
                .ritualPackId(entity.getRitualPackId())
                .recommendationId(entity.getRecommendationId())
                .status(entity.getStatus())
                .feedback(entity.getFeedback())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
