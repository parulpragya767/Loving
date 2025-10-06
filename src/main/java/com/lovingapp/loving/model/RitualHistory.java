package com.lovingapp.loving.model;

import com.lovingapp.loving.model.enums.EmojiFeedback;
import com.lovingapp.loving.model.enums.RitualHistoryStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RitualHistoryStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback", length = 30)
    private EmojiFeedback feedback;

    // When this history event occurred (e.g., when completed/skipped)
    @Column(name = "occurred_at", columnDefinition = "timestamptz")
    private OffsetDateTime occurredAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, columnDefinition = "timestamptz")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamptz")
    private OffsetDateTime updatedAt;
}
