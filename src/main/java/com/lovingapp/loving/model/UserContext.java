package com.lovingapp.loving.model;

import com.lovingapp.loving.model.enums.*;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a user's context for ritual matching.
 * Captures the user's current state, preferences, and situational context.
 */
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

    // User and conversation identification
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "conversation_id")
    private String conversationId;

    // Core context dimensions
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

    // Ritual preferences
    @Type(JsonType.class)
    @Column(name = "preferred_ritual_types", columnDefinition = "jsonb")
    @Builder.Default
    private List<RitualType> preferredRitualTypes = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "preferred_tones", columnDefinition = "jsonb")
    @Builder.Default
    private List<RitualTone> preferredTones = new ArrayList<>();

    // Practical constraints
    @Column(name = "available_time_minutes")
    private Integer availableTimeMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_effort_level", length = 20)
    private EffortLevel preferredEffortLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_intensity", length = 20)
    private IntensityLevel preferredIntensity;

    // Situational context
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

    // Semantic understanding
    @Column(name = "semantic_query", columnDefinition = "text")
    private String semanticQuery;

    // Metadata
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
