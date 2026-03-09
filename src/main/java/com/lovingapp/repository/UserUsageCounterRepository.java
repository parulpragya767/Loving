package com.lovingapp.repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lovingapp.model.entity.UserUsageCounter;
import com.lovingapp.model.enums.UsagePeriodType;

public interface UserUsageCounterRepository extends JpaRepository<UserUsageCounter, UUID> {

    Optional<UserUsageCounter> findByUserIdAndPeriodTypeAndPeriodStart(UUID userId, UsagePeriodType periodType,
            OffsetDateTime periodStart);
}
