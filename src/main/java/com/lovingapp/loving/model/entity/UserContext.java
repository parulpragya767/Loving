package com.lovingapp.loving.model.entity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import com.lovingapp.loving.model.enums.Journey;
import com.lovingapp.loving.model.enums.LoveType;
import com.lovingapp.loving.model.enums.RelationalNeed;
import com.lovingapp.loving.model.enums.RelationshipStatus;
import com.vladmihalcea.hibernate.type.json.JsonType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "conversation_id", columnDefinition = "uuid")
    private UUID conversationId;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private Journey journey;

    @Type(JsonType.class)
    @Column(name = "love_types", columnDefinition = "jsonb")
    @Builder.Default
    private List<LoveType> loveTypes = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "relational_needs", columnDefinition = "jsonb")
    @Builder.Default
    private List<RelationalNeed> relationalNeeds = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_status", length = 30)
    private RelationshipStatus relationshipStatus;

    @Column(name = "semantic_summary", columnDefinition = "text")
    private String semanticSummary;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "timestamptz", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamptz", nullable = false)
    private OffsetDateTime updatedAt;
}
