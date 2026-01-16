package com.lovingapp.loving.model.entity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import com.lovingapp.loving.model.domain.MediaAsset;
import com.lovingapp.loving.model.enums.Journey;
import com.lovingapp.loving.model.enums.LoveType;
import com.lovingapp.loving.model.enums.PublicationStatus;
import com.lovingapp.loving.model.enums.RelationalNeed;
import com.vladmihalcea.hibernate.type.json.JsonType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ritual_packs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RitualPack {
    @Id
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @NotNull
    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "tag_line", length = 255)
    private String tagLine;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "how_it_helps", columnDefinition = "text")
    private String howItHelps;

    // Curated rituals in this pack
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "ritual_pack_rituals", joinColumns = @JoinColumn(name = "pack_id", nullable = false), inverseJoinColumns = @JoinColumn(name = "ritual_id", nullable = false))
    @Builder.Default
    private List<Ritual> rituals = new ArrayList<>();

    // Core tags for recommendation and classification
    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private Journey journey;

    @Type(JsonType.class)
    @Column(name = "love_types", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private List<LoveType> loveTypes = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "relational_needs", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private List<RelationalNeed> relationalNeeds = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "media_assets", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private List<MediaAsset> mediaAssets = new ArrayList<>();

    @Column(name = "semantic_summary", columnDefinition = "text")
    private String semanticSummary;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PublicationStatus status;

    @Column(name = "content_hash", length = 128)
    private String contentHash;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    private OffsetDateTime updatedAt;
}
