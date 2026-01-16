package com.lovingapp.loving.model.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.lovingapp.loving.model.enums.RitualFeedback;
import com.lovingapp.loving.model.enums.RitualHistoryStatus;

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
@Table(name = "ritual_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RitualHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @NotNull
    @Column(name = "ritual_id", nullable = false, columnDefinition = "uuid")
    private UUID ritualId;

    @Column(name = "ritual_pack_id", columnDefinition = "uuid")
    private UUID ritualPackId;

    @Column(name = "recommendation_id", columnDefinition = "uuid")
    private UUID recommendationId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RitualHistoryStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback", length = 30)
    private RitualFeedback feedback;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    private OffsetDateTime updatedAt;
}
