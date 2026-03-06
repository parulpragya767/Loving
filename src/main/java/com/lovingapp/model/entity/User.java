package com.lovingapp.model.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.lovingapp.model.enums.SubscriptionSource;
import com.lovingapp.model.enums.SubscriptionStatus;
import com.lovingapp.model.enums.SubscriptionTier;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @Column(name = "auth_user_id", nullable = false, unique = true, columnDefinition = "uuid")
    private UUID authUserId;

    @NotNull
    @Email
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "display_name", length = 120)
    private String displayName;

    @NotNull
    @Column(name = "onboarding_completed", nullable = false)
    @Builder.Default
    private Boolean onboardingCompleted = false;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_tier", length = 30, nullable = false)
    @Builder.Default
    private SubscriptionTier subscriptionTier = SubscriptionTier.FREE;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status", length = 30, nullable = false)
    @Builder.Default
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.INACTIVE;

    @Column(name = "subscription_expires_at", columnDefinition = "timestamptz")
    private OffsetDateTime subscriptionExpiresAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_source", length = 30, nullable = false)
    @Builder.Default
    private SubscriptionSource subscriptionSource = SubscriptionSource.NONE;

    @Column(name = "subscription_started_at", columnDefinition = "timestamptz")
    private OffsetDateTime subscriptionStartedAt;

    @NotNull
    @Column(name = "is_beta_user", nullable = false)
    @Builder.Default
    private Boolean isBetaUser = false;

    @Column(name = "last_login_at", columnDefinition = "timestamptz")
    private OffsetDateTime lastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false, columnDefinition = "timestamptz")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamptz", nullable = false)
    private OffsetDateTime updatedAt;
}
