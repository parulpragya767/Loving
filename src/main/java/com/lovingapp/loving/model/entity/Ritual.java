package com.lovingapp.loving.model.entity;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

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
import com.vladmihalcea.hibernate.type.json.JsonType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rituals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ritual {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "full_description", columnDefinition = "text")
    private String fullDescription;

    @Type(JsonType.class)
    @Column(name = "ritual_types", columnDefinition = "jsonb")
    @Builder.Default
    private List<RitualType> ritualTypes = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "ritual_mode", length = 20, nullable = false)
    private RitualMode ritualMode;

    @Type(JsonType.class)
    @Column(name = "ritual_tones", columnDefinition = "jsonb")
    @Builder.Default
    private List<RitualTone> ritualTones = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "sensitivity_level", length = 20)
    private SensitivityLevel sensitivityLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "effort_level", length = 20)
    private EffortLevel effortLevel;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @Transient
    public Duration getEstimatedDuration() {
        return estimatedDurationMinutes != null ? Duration.ofMinutes(estimatedDurationMinutes) : null;
    }

    public void setEstimatedDuration(Duration duration) {
        this.estimatedDurationMinutes = duration != null ? (int) duration.toMinutes() : null;
    }

    @Type(JsonType.class)
    @Column(name = "ritual_steps", columnDefinition = "jsonb")
    @Builder.Default
    private List<RitualStep> ritualSteps = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "media_assets", columnDefinition = "jsonb")
    @Builder.Default
    private List<MediaAsset> mediaAssets = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "love_types", columnDefinition = "jsonb")
    @Builder.Default
    private List<LoveType> loveTypesSupported = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "emotional_states", columnDefinition = "jsonb")
    @Builder.Default
    private List<EmotionalState> emotionalStatesSupported = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "relational_needs", columnDefinition = "jsonb")
    @Builder.Default
    private List<RelationalNeed> relationalNeedsServed = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "life_contexts", columnDefinition = "jsonb")
    @Builder.Default
    private List<LifeContext> lifeContextsRelevant = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "rhythm", length = 20)
    private Rhythm rhythm;

    @Type(JsonType.class)
    @Column(name = "preparation_requirements", columnDefinition = "jsonb")
    @Builder.Default
    private List<String> preparationRequirements = new ArrayList<>();

    @Column(name = "semantic_summary", columnDefinition = "text")
    private String semanticSummary;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PublicationStatus status;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "timestamptz")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamptz")
    private OffsetDateTime updatedAt;
}