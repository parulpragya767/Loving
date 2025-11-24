package com.lovingapp.loving.model.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.lovingapp.loving.model.enums.EmojiFeedback;
import com.lovingapp.loving.model.enums.RitualHistoryStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    @GeneratedValue
    private UUID id;

    // We store foreign keys as UUIDs to keep the entity decoupled.
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "ritual_id", nullable = false, columnDefinition = "uuid")
    private UUID ritualId;

    @Column(name = "ritual_pack_id", columnDefinition = "uuid")
    private UUID ritualPackId;

    @Column(name = "recommendation_id", columnDefinition = "uuid")
    private UUID recommendationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RitualHistoryStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback", length = 30)
    private EmojiFeedback feedback;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, columnDefinition = "timestamptz")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamptz")
    private OffsetDateTime updatedAt;
}
