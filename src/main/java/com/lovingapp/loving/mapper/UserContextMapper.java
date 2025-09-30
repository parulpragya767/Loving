package com.lovingapp.loving.mapper;

import com.lovingapp.loving.model.UserContext;
import com.lovingapp.loving.model.dto.UserContextDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

/**
 * Mapper for converting between UserContext entity and DTO.
 */
@Component
public class UserContextMapper {

    /**
     * Converts a UserContext entity to a UserContextDTO.
     * @param entity the entity to convert
     * @return the converted DTO, or null if the input is null
     */
    public UserContextDTO toDto(UserContext entity) {
        if (entity == null) {
            return null;
        }
        
        return UserContextDTO.builder()
                .id(entity.getId() != null ? entity.getId().toString() : null)
                .userId(entity.getUserId())
                .conversationId(entity.getConversationId())
                .emotionalStates(Objects.requireNonNullElse(entity.getEmotionalStates(), Collections.emptyList()))
                .relationalNeeds(Objects.requireNonNullElse(entity.getRelationalNeeds(), Collections.emptyList()))
                .preferredLoveLanguages(Objects.requireNonNullElse(entity.getPreferredLoveLanguages(), Collections.emptyList()))
                .preferredRitualTypes(Objects.requireNonNullElse(entity.getPreferredRitualTypes(), Collections.emptyList()))
                .preferredTones(Objects.requireNonNullElse(entity.getPreferredTones(), Collections.emptyList()))
                .availableTimeMinutes(entity.getAvailableTimeMinutes())
                .preferredEffortLevel(entity.getPreferredEffortLevel())
                .preferredIntensity(entity.getPreferredIntensity())
                .currentContexts(Objects.requireNonNullElse(entity.getCurrentContexts(), Collections.emptyList()))
                .timeContext(entity.getTimeContext())
                .relationshipStatus(entity.getRelationshipStatus())
                .semanticQuery(entity.getSemanticQuery())
                .lastInteractionAt(entity.getLastInteractionAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Converts a UserContextDTO to a UserContext entity.
     * @param dto the DTO to convert
     * @return the converted entity, or null if the input is null
     */
    public UserContext toEntity(UserContextDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return UserContext.builder()
                .id(dto.getId() != null ? UUID.fromString(dto.getId()) : null)
                .userId(dto.getUserId())
                .conversationId(dto.getConversationId())
                .emotionalStates(Objects.requireNonNullElse(dto.getEmotionalStates(), Collections.emptyList()))
                .relationalNeeds(Objects.requireNonNullElse(dto.getRelationalNeeds(), Collections.emptyList()))
                .preferredLoveLanguages(Objects.requireNonNullElse(dto.getPreferredLoveLanguages(), Collections.emptyList()))
                .preferredRitualTypes(Objects.requireNonNullElse(dto.getPreferredRitualTypes(), Collections.emptyList()))
                .preferredTones(Objects.requireNonNullElse(dto.getPreferredTones(), Collections.emptyList()))
                .availableTimeMinutes(dto.getAvailableTimeMinutes())
                .preferredEffortLevel(dto.getPreferredEffortLevel())
                .preferredIntensity(dto.getPreferredIntensity())
                .currentContexts(Objects.requireNonNullElse(dto.getCurrentContexts(), Collections.emptyList()))
                .timeContext(dto.getTimeContext())
                .relationshipStatus(dto.getRelationshipStatus())
                .semanticQuery(dto.getSemanticQuery())
                .lastInteractionAt(dto.getLastInteractionAt())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    /**
     * Updates an existing UserContext entity with values from a DTO.
     * @param entity the entity to update
     * @param dto the DTO containing the new values
     */
    public void updateEntityFromDto(UserContext entity, UserContextDTO dto) {
        if (dto == null || entity == null) {
            return;
        }
        
        if (dto.getUserId() != null) entity.setUserId(dto.getUserId());
        if (dto.getConversationId() != null) entity.setConversationId(dto.getConversationId());
        
        // Core context dimensions
        entity.setEmotionalStates(Objects.requireNonNullElse(dto.getEmotionalStates(), Collections.emptyList()));
        entity.setRelationalNeeds(Objects.requireNonNullElse(dto.getRelationalNeeds(), Collections.emptyList()));
        entity.setPreferredLoveLanguages(Objects.requireNonNullElse(dto.getPreferredLoveLanguages(), Collections.emptyList()));
        
        // Ritual preferences
        entity.setPreferredRitualTypes(Objects.requireNonNullElse(dto.getPreferredRitualTypes(), Collections.emptyList()));
        entity.setPreferredTones(Objects.requireNonNullElse(dto.getPreferredTones(), Collections.emptyList()));
        
        // Practical constraints
        if (dto.getAvailableTimeMinutes() != null) entity.setAvailableTimeMinutes(dto.getAvailableTimeMinutes());
        if (dto.getPreferredEffortLevel() != null) entity.setPreferredEffortLevel(dto.getPreferredEffortLevel());
        if (dto.getPreferredIntensity() != null) entity.setPreferredIntensity(dto.getPreferredIntensity());
        
        // Situational context
        entity.setCurrentContexts(Objects.requireNonNullElse(dto.getCurrentContexts(), Collections.emptyList()));
        if (dto.getTimeContext() != null) entity.setTimeContext(dto.getTimeContext());
        if (dto.getRelationshipStatus() != null) entity.setRelationshipStatus(dto.getRelationshipStatus());
        
        // Semantic understanding
        if (dto.getSemanticQuery() != null) entity.setSemanticQuery(dto.getSemanticQuery());
        
        // Metadata updates are handled by @PrePersist and @PreUpdate
    }
}
