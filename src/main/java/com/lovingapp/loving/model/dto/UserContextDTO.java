package com.lovingapp.loving.model.dto;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.lovingapp.loving.model.enums.EffortLevel;
import com.lovingapp.loving.model.enums.EmotionalState;
import com.lovingapp.loving.model.enums.IntensityLevel;
import com.lovingapp.loving.model.enums.LifeContext;
import com.lovingapp.loving.model.enums.LoveType;
import com.lovingapp.loving.model.enums.RelationalNeed;
import com.lovingapp.loving.model.enums.RelationshipStatus;
import com.lovingapp.loving.model.enums.RitualTone;
import com.lovingapp.loving.model.enums.RitualType;
import com.lovingapp.loving.model.enums.TimeContext;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserContextDTO {
    private String id;

    private UUID userId;
    private String conversationId;

    @Builder.Default
    private List<EmotionalState> emotionalStates = new ArrayList<>();

    @Builder.Default
    private List<RelationalNeed> relationalNeeds = new ArrayList<>();

    @Builder.Default
    private List<LoveType> preferredLoveLanguages = new ArrayList<>();

    @Builder.Default
    private List<RitualType> preferredRitualTypes = new ArrayList<>();

    @Builder.Default
    private List<RitualTone> preferredTones = new ArrayList<>();

    private Integer availableTimeMinutes;
    private EffortLevel preferredEffortLevel;
    private IntensityLevel preferredIntensity;

    @Builder.Default
    private List<LifeContext> currentContexts = new ArrayList<>();

    private TimeContext timeContext;
    private RelationshipStatus relationshipStatus;
    private String semanticQuery;
    private OffsetDateTime lastInteractionAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
