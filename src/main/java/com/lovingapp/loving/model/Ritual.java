package com.lovingapp.loving.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vladmihalcea.hibernate.type.json.JsonType;
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
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rituals")
@Getter
@Setter
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

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ritual_types", joinColumns = @JoinColumn(name = "ritual_id"))
    @Column(name = "ritual_type")
    private List<RitualType> ritualTypes = new ArrayList<>();

    // ritual_mode: enum(solo, partner, group)
    @Enumerated(EnumType.STRING)
    @Column(name = "ritual_mode")
    private RitualMode ritualMode;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ritual_tones", joinColumns = @JoinColumn(name = "ritual_id"))
    @Column(name = "tone")
    private List<RitualTone> tones = new ArrayList<>();

    // sensitivity_level: enum(low, moderate, high)
    @Enumerated(EnumType.STRING)
    @Column(name = "sensitivity_level", length = 20)
    private SensitivityLevel sensitivityLevel;

    // effort_level: enum(low, moderate, high)
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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ritual_steps", joinColumns = @JoinColumn(name = "ritual_id"))
    @OrderColumn(name = "ritual_step_order")
    private List<RitualStep> ritualSteps = new ArrayList<>();
    // media_assets: jsonb nullable
    @Type(JsonType.class)
    @Column(name = "media_assets", columnDefinition = "jsonb")
    private List<MediaAsset> mediaAssets = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ritual_love_types", joinColumns = @JoinColumn(name = "ritual_id"))
    @Column(name = "love_type")
    private List<LoveType> loveTypesSupported = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ritual_emotional_states", joinColumns = @JoinColumn(name = "ritual_id"))
    @Column(name = "emotional_state")
    private List<EmotionalState> emotionalStatesSupported = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ritual_relational_needs", joinColumns = @JoinColumn(name = "ritual_id"))
    @Column(name = "relational_need")
    private List<RelationalNeed> relationalNeedsServed = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ritual_life_contexts", joinColumns = @JoinColumn(name = "ritual_id"))
    @Column(name = "life_context")
    private List<LifeContext> lifeContextsRelevant = new ArrayList<>();

    // rhythm: enum(daily, weekly, occasional, event_triggered)
    @Enumerated(EnumType.STRING)
    @Column(name = "rhythm", length = 30)
    private Rhythm rhythm;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ritual_preparation_requirements", joinColumns = @JoinColumn(name = "ritual_id"))
    @Column(name = "requirement")
    private List<String> preparationRequirements = new ArrayList<>();

    // semantic_summary: text, nullable
    @Column(name = "semantic_summary", columnDefinition = "text")
    private String semanticSummary;

    // status: enum(published, draft, archived)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
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

    // Enum definitions have been moved to separate files in the enums package

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MediaAsset {
        private String type; // e.g., "image", "audio"
        private String url;
    }
}