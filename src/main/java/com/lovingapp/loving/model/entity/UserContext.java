package com.lovingapp.loving.model.entity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.Type;

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
import com.vladmihalcea.hibernate.type.json.JsonType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_contexts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserContext {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "conversation_id")
    private String conversationId;

    @Type(JsonType.class)
    @Column(name = "emotional_states", columnDefinition = "jsonb")
    @Builder.Default
    private List<EmotionalState> emotionalStates = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "relational_needs", columnDefinition = "jsonb")
    @Builder.Default
    private List<RelationalNeed> relationalNeeds = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "preferred_love_languages", columnDefinition = "jsonb")
    @Builder.Default
    private List<LoveType> preferredLoveLanguages = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "preferred_ritual_types", columnDefinition = "jsonb")
    @Builder.Default
    private List<RitualType> preferredRitualTypes = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "preferred_tones", columnDefinition = "jsonb")
    @Builder.Default
    private List<RitualTone> preferredTones = new ArrayList<>();

    @Column(name = "available_time_minutes")
    private Integer availableTimeMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_effort_level", length = 20)
    private EffortLevel preferredEffortLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_intensity", length = 20)
    private IntensityLevel preferredIntensity;

    @Type(JsonType.class)
    @Column(name = "current_contexts", columnDefinition = "jsonb")
    @Builder.Default
    private List<LifeContext> currentContexts = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "time_context", length = 20)
    private TimeContext timeContext;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_status", length = 30)
    private RelationshipStatus relationshipStatus;

    @Column(name = "semantic_query", columnDefinition = "text")
    private String semanticQuery;

    @Column(name = "last_interaction_at", columnDefinition = "timestamptz")
    private OffsetDateTime lastInteractionAt;

    @Column(name = "created_at", columnDefinition = "timestamptz", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "timestamptz")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.lastInteractionAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
