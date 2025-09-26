package com.lovingapp.loving.dto;

import com.lovingapp.loving.model.RitualStep;
import com.lovingapp.loving.model.MediaAsset;
import com.lovingapp.loving.model.enums.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class RitualDTO {
    private UUID id;
    private String title;
    private String shortDescription;
    private String fullDescription;
    private List<RitualType> ritualTypes;
    private RitualMode ritualMode;
    private List<RitualTone> ritualTones;
    private SensitivityLevel sensitivityLevel;
    private EffortLevel effortLevel;
    private Integer estimatedDurationMinutes;
    private List<RitualStep> ritualSteps;
    private List<MediaAsset> mediaAssets;
    private List<LoveType> loveTypesSupported;
    private List<EmotionalState> emotionalStatesSupported;
    private List<RelationalNeed> relationalNeedsServed;
    private List<LifeContext> lifeContextsRelevant;
    private Rhythm rhythm;
    private List<String> preparationRequirements;
    private String semanticSummary;
    private PublicationStatus status;
    private String createdBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

