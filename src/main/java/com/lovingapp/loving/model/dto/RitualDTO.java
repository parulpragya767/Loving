package com.lovingapp.loving.model.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.lovingapp.loving.model.domain.MediaAsset;
import com.lovingapp.loving.model.domain.RitualStep;
import com.lovingapp.loving.model.enums.EffortLevel;
import com.lovingapp.loving.model.enums.EmotionalState;
import com.lovingapp.loving.model.enums.LifeContext;
import com.lovingapp.loving.model.enums.LoveType;
import com.lovingapp.loving.model.enums.PublicationStatus;
import com.lovingapp.loving.model.enums.RelationalNeed;
import com.lovingapp.loving.model.enums.Rhythm;
import com.lovingapp.loving.model.enums.RitualMode;
import com.lovingapp.loving.model.enums.RitualTone;
import com.lovingapp.loving.model.enums.RitualType;
import com.lovingapp.loving.model.enums.SensitivityLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
