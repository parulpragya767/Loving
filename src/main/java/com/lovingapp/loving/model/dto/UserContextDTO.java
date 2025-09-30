package com.lovingapp.loving.model.dto;

import com.lovingapp.loving.model.enums.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the user's current context for ritual matching.
 * Captures the user's emotional state, preferences, and situational context.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserContextDTO {
    private String id;
    
    // User and conversation identification
    private String userId;
    private String conversationId;
    
    // Core context dimensions
    @Builder.Default
    private List<EmotionalState> emotionalStates = new ArrayList<>();
    @Builder.Default
    private List<RelationalNeed> relationalNeeds = new ArrayList<>();
    @Builder.Default
    private List<LoveType> preferredLoveLanguages = new ArrayList<>();
    
    // Ritual preferences
    @Builder.Default
    private List<RitualType> preferredRitualTypes = new ArrayList<>();
    @Builder.Default
    private List<RitualTone> preferredTones = new ArrayList<>();
    
    // Practical constraints
    private Integer availableTimeMinutes;
    private EffortLevel preferredEffortLevel;
    private IntensityLevel preferredIntensity;
    
    // Situational context
    @Builder.Default
    private List<LifeContext> currentContexts = new ArrayList<>();
    private TimeContext timeContext; // e.g., MORNING, EVENING, WEEKEND
    private RelationshipStatus relationshipStatus;
    
    // Semantic understanding
    private String semanticQuery; // Raw user input for semantic matching
    
    // Metadata
    private OffsetDateTime lastInteractionAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
