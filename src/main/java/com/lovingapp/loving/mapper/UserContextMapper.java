package com.lovingapp.loving.mapper;

import java.util.Collections;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.lovingapp.loving.model.dto.UserContextDTOs.UserContextDTO;
import com.lovingapp.loving.model.entity.UserContext;

@Component
public final class UserContextMapper {
    public static UserContextDTO toDto(UserContext entity) {
        if (entity == null) {
            return null;
        }

        return UserContextDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .conversationId(entity.getConversationId())
                .journey(entity.getJourney())
                .loveTypes(Objects.requireNonNullElse(entity.getLoveTypes(), Collections.emptyList()))
                .relationalNeeds(Objects.requireNonNullElse(entity.getRelationalNeeds(), Collections.emptyList()))
                .relationshipStatus(entity.getRelationshipStatus())
                .semanticSummary(entity.getSemanticSummary())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
