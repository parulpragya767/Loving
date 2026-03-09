package com.lovingapp.mapper;

import com.lovingapp.model.dto.UserUsageCounterDTOs.UserUsageCounterDTO;
import com.lovingapp.model.entity.UserUsageCounter;

public final class UserUsageCounterMapper {
    private UserUsageCounterMapper() {
    }

    public static UserUsageCounterDTO toDto(UserUsageCounter entity) {
        if (entity == null)
            return null;
        return UserUsageCounterDTO.builder()
                .id(entity.getId())
                .periodType(entity.getPeriodType())
                .periodStart(entity.getPeriodStart())
                .aiMessagesCount(entity.getAiMessagesCount())
                .recommendationsCount(entity.getRecommendationsCount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
