package com.lovingapp.loving.mapper;

import com.lovingapp.loving.dto.RitualHistoryDTO;
import com.lovingapp.loving.model.RitualHistory;

public final class RitualHistoryMapper {
    private RitualHistoryMapper() {}

    public static RitualHistoryDTO toDto(RitualHistory entity) {
        if (entity == null) return null;
        return RitualHistoryDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .ritualId(entity.getRitualId())
                .status(entity.getStatus())
                .feedback(entity.getFeedback())
                .occurredAt(entity.getOccurredAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static RitualHistory fromDto(RitualHistoryDTO dto) {
        if (dto == null) return null;
        return RitualHistory.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .ritualId(dto.getRitualId())
                .status(dto.getStatus())
                .feedback(dto.getFeedback())
                .occurredAt(dto.getOccurredAt())
                .build();
    }

    public static void updateEntityFromDto(RitualHistoryDTO dto, RitualHistory entity) {
        if (dto == null || entity == null) return;
        entity.setUserId(dto.getUserId());
        entity.setRitualId(dto.getRitualId());
        entity.setStatus(dto.getStatus());
        entity.setFeedback(dto.getFeedback());
        entity.setOccurredAt(dto.getOccurredAt());
    }
}
