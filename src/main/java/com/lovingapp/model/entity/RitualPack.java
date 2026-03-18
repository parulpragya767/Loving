package com.lovingapp.model.entity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import com.lovingapp.model.domain.MediaAsset;
import com.lovingapp.model.enums.Journey;
import com.lovingapp.model.enums.LoveType;
import com.lovingapp.model.enums.PublicationStatus;
import com.lovingapp.model.enums.RelationalNeed;
import com.vladmihalcea.hibernate.type.json.JsonType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

    @Column(name = "short_description", columnDefinition = "text")
    private String shortDescription;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "how_it_helps", columnDefinition = "text")
    private String howItHelps;

    // Curated rituals in this pack with ordering
    @OneToMany(mappedBy = "ritualPack", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RitualPackRitual> ritualPackRituals = new ArrayList<>();

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

    public void setRitualsFromList(List<Ritual> rituals) {
        this.ritualPackRituals.clear();
        if (rituals != null && !rituals.isEmpty()) {
            for (int i = 0; i < rituals.size(); i++) {
                Ritual ritual = rituals.get(i);
                RitualPackRitualId id = new RitualPackRitualId(this.id, ritual.getId());
                RitualPackRitual rpr = RitualPackRitual.builder()
                        .id(id)
                        .ritualPack(this)
                        .ritual(ritual)
                        .position(i)
                        .build();
                this.ritualPackRituals.add(rpr);
            }
        }
    }
}
