package com.lovingapp.loving.model;
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
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rituals")
@Data 
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ritual {
    @Id
    @GeneratedValue()
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "full_description", columnDefinition = "text")
    private String fullDescription;

    @Type(JsonType.class)
    @Column(name = "ritual_type", columnDefinition = "jsonb")
    @Builder.Default
    private List<RitualType> ritualTypes = new ArrayList<>();

    // ritual_mode: enum(solo, partner, group)
    @Enumerated(EnumType.STRING)
    @Column(name = "ritual_mode")
    private RitualMode ritualMode;

    @Type(JsonType.class)
    @Column(name = "tone", columnDefinition = "jsonb")
    @Builder.Default
    private List<RitualTone> tones = new ArrayList<>();

    // sensitivity_level: enum(low, moderate, high)
    @Type(JsonType.class)
    @Column(name = "sensitivity_level", columnDefinition = "jsonb")
    private SensitivityLevel sensitivityLevel;

    // effort_level: enum(low, moderate, high)
    @Type(JsonType.class)
    @Column(name = "effort_level", columnDefinition = "jsonb")
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

    // media_assets: jsonb nullable
    @Type(JsonType.class)
    @Column(name = "media_assets", columnDefinition = "jsonb")
    @Builder.Default
    private List<MediaAsset> mediaAssets = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "love_type", columnDefinition = "jsonb")
    @Builder.Default
    private List<LoveType> loveTypesSupported = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "emotional_state", columnDefinition = "jsonb")
    @Builder.Default
    private List<EmotionalState> emotionalStatesSupported = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "relational_need", columnDefinition = "jsonb")
    @Builder.Default
    private List<RelationalNeed> relationalNeedsServed = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "life_context", columnDefinition = "jsonb")
    @Builder.Default
    private List<LifeContext> lifeContextsRelevant = new ArrayList<>();

    // rhythm: enum(daily, weekly, occasional, event_triggered)
    @Type(JsonType.class)
    @Column(name = "rhythm", columnDefinition = "jsonb")
    private Rhythm rhythm;

    @Type(JsonType.class)
    @Column(name = "requirement", columnDefinition = "jsonb")
    @Builder.Default
    private List<String> preparationRequirements = new ArrayList<>();

    // semantic_summary: text, nullable
    @Column(name = "semantic_summary", columnDefinition = "text")
    private String semanticSummary;

    // status: enum(published, draft, archived)
    @Type(JsonType.class)
    @Column(name = "status", columnDefinition = "jsonb")
    private PublicationStatus status;

    // created_by
    @Column(name = "created_by", length = 100)
    private String createdBy;

    // timestamps
    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "timestamptz")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamptz")
    private OffsetDateTime updatedAt;
}