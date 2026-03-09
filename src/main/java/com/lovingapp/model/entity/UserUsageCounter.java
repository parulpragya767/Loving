package com.lovingapp.model.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.lovingapp.model.enums.UsagePeriodType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_usage_counters", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "period_type", "period_start" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUsageCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 20)
    private UsagePeriodType periodType;

    @NotNull
    @Column(name = "period_start", nullable = false, columnDefinition = "timestamptz")
    private OffsetDateTime periodStart;

    @Builder.Default
    @Column(name = "ai_messages_count", nullable = false)
    private Integer aiMessagesCount = 0;

    @Builder.Default
    @Column(name = "recommendations_count", nullable = false)
    private Integer recommendationsCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    private OffsetDateTime updatedAt;
}
