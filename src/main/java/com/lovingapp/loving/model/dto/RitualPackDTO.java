package com.lovingapp.loving.model.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.lovingapp.loving.model.enums.EffortLevel;
import com.lovingapp.loving.model.enums.EmotionalState;
import com.lovingapp.loving.model.enums.LifeContext;
import com.lovingapp.loving.model.enums.LoveType;
import com.lovingapp.loving.model.enums.PublicationStatus;
import com.lovingapp.loving.model.enums.RelationalNeed;
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
public class RitualPackDTO {
    private UUID id;
    private String title;
    private String shortDescription;
    private String fullDescription;

    // Ritual IDs to include in the pack (used for create/update)
    private List<UUID> ritualIds;

    // Aggregated tags
    private List<RitualType> ritualTypes;
    private List<RitualTone> ritualTones;
    private SensitivityLevel sensitivityLevel;
    private EffortLevel effortLevel;
    private List<LoveType> loveTypesSupported;
    private List<EmotionalState> emotionalStatesSupported;
    private List<RelationalNeed> relationalNeedsServed;
    private List<LifeContext> lifeContextsRelevant;

    private String semanticSummary;
    private PublicationStatus status;
    private String createdBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
