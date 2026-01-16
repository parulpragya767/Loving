package com.lovingapp.loving.model.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.lovingapp.loving.model.enums.RecommendationSource;
import com.lovingapp.loving.model.enums.RecommendationStatus;

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
@Table(name = "ritual_recommendations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RitualRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 20, nullable = false)
    private RecommendationSource source;

    @Column(name = "source_id", columnDefinition = "uuid")
    private UUID sourceId;

    @NotNull
    @Column(name = "ritual_pack_id", nullable = false, columnDefinition = "uuid")
    private UUID ritualPackId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private RecommendationStatus status;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "timestamptz", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
