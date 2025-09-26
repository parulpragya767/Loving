package com.lovingapp.loving.dto;

import com.lovingapp.loving.model.Ritual;
import com.lovingapp.loving.model.RitualStep;
import com.lovingapp.loving.model.MediaAsset;
import com.lovingapp.loving.model.enums.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.OffsetDateTime;

import java.util.List;
import java.util.ArrayList;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class RitualDTO {
    private String id;
    private String title;
    private String shortDescription;
    private String fullDescription;
    private List<RitualType> ritualTypes = new ArrayList<>();
    private RitualMode ritualMode;
    private List<RitualTone> tones = new ArrayList<>();
    private SensitivityLevel sensitivityLevel;
    private EffortLevel effortLevel;
    private Integer estimatedDurationMinutes;
    private List<RitualStep> ritualSteps = new ArrayList<>();
    private List<MediaAsset> mediaAssets = new ArrayList<>();
    private List<LoveType> loveTypesSupported = new ArrayList<>();
    private List<EmotionalState> emotionalStatesSupported = new ArrayList<>();
    private List<RelationalNeed> relationalNeedsServed = new ArrayList<>();
    private List<LifeContext> lifeContextsRelevant = new ArrayList<>();
    private Rhythm rhythm;
    private List<String> preparationRequirements = new ArrayList<>();
    private String semanticSummary;
    private PublicationStatus status;
    private String createdBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public RitualDTO(Ritual ritual) {
        this.id = ritual.getId() != null ? ritual.getId().toString() : null;
        this.title = ritual.getTitle();
        this.shortDescription = ritual.getShortDescription();
        this.fullDescription = ritual.getFullDescription();
        this.ritualTypes = ritual.getRitualTypes();
        this.ritualMode = ritual.getRitualMode();
        this.tones = ritual.getTones();
        this.sensitivityLevel = ritual.getSensitivityLevel();
        this.effortLevel = ritual.getEffortLevel();
        this.estimatedDurationMinutes = ritual.getEstimatedDurationMinutes();
        this.ritualSteps = ritual.getRitualSteps();
        this.mediaAssets = ritual.getMediaAssets();
        this.loveTypesSupported = ritual.getLoveTypesSupported();
        this.emotionalStatesSupported = ritual.getEmotionalStatesSupported();
        this.relationalNeedsServed = ritual.getRelationalNeedsServed();
        this.lifeContextsRelevant = ritual.getLifeContextsRelevant();
        this.rhythm = ritual.getRhythm();
        this.preparationRequirements = ritual.getPreparationRequirements();
        this.semanticSummary = ritual.getSemanticSummary();
        this.status = ritual.getStatus();
        this.createdBy = ritual.getCreatedBy();
        this.createdAt = ritual.getCreatedAt();
        this.updatedAt = ritual.getUpdatedAt();
    }

    // Helper methods for duration conversion
    public Duration getEstimatedDuration() {
        return estimatedDurationMinutes != null ? Duration.ofMinutes(estimatedDurationMinutes) : null;
    }

    public void setEstimatedDuration(Duration duration) {
        this.estimatedDurationMinutes = duration != null ? (int) duration.toMinutes() : null;
    }
}
