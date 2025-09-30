package com.lovingapp.loving.model;

import com.lovingapp.loving.model.enums.*;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.*;

@Entity
@Table(name = "ritual_packs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RitualPack {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "full_description", columnDefinition = "text")
    private String fullDescription;

    // Curated rituals in this pack
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "ritual_pack_rituals",
            joinColumns = @JoinColumn(name = "pack_id"),
            inverseJoinColumns = @JoinColumn(name = "ritual_id")
    )
    @Builder.Default
    private List<Ritual> rituals = new ArrayList<>();

    // Aggregated tags computed from child rituals
    @Type(JsonType.class)
    @Column(name = "ritual_types", columnDefinition = "jsonb")
    @Builder.Default
    private List<RitualType> ritualTypes = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "ritual_tones", columnDefinition = "jsonb")
    @Builder.Default
    private List<RitualTone> ritualTones = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "sensitivity_level", length = 20)
    private SensitivityLevel sensitivityLevel; // optional: dominant/most common

    @Enumerated(EnumType.STRING)
    @Column(name = "effort_level", length = 20)
    private EffortLevel effortLevel; // optional: average/representative

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
