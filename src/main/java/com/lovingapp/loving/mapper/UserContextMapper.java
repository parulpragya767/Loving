package com.lovingapp.loving.mapper;

import java.util.Collections;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.lovingapp.loving.model.dto.UserContextDTO;
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

    public static UserContext toEntity(UserContextDTO dto) {
        if (dto == null) {
            return null;
        }

        return UserContext.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .conversationId(dto.getConversationId())
                .journey(dto.getJourney())
                .loveTypes(Objects.requireNonNullElse(dto.getLoveTypes(), Collections.emptyList()))
                .relationalNeeds(Objects.requireNonNullElse(dto.getRelationalNeeds(), Collections.emptyList()))
                .relationshipStatus(dto.getRelationshipStatus())
                .semanticSummary(dto.getSemanticSummary())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public static void updateEntityFromDto(UserContext entity, UserContextDTO dto) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.getUserId() != null)
            entity.setUserId(dto.getUserId());
        if (dto.getConversationId() != null)
            entity.setConversationId(dto.getConversationId());
        entity.setJourney(dto.getJourney());
        entity.setLoveTypes(Objects.requireNonNullElse(dto.getLoveTypes(), Collections.emptyList()));
        entity.setRelationalNeeds(Objects.requireNonNullElse(dto.getRelationalNeeds(), Collections.emptyList()));
        if (dto.getRelationshipStatus() != null)
            entity.setRelationshipStatus(dto.getRelationshipStatus());

        if (dto.getSemanticSummary() != null)
            entity.setSemanticSummary(dto.getSemanticSummary());
    }
}
