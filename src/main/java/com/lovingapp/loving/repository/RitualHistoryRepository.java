package com.lovingapp.loving.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lovingapp.loving.model.entity.RitualHistory;
import com.lovingapp.loving.model.enums.RitualHistoryStatus;

public interface RitualHistoryRepository extends JpaRepository<RitualHistory, UUID> {
    List<RitualHistory> findByUserIdOrderByUpdatedAtDesc(UUID userId);

    Optional<RitualHistory> findByIdAndUserId(UUID id, UUID userId);

    List<RitualHistory> findByUserIdAndRecommendationId(UUID userId, UUID recommendationId);

    List<RitualHistory> findByUserIdAndStatusInOrderByUpdatedAtDesc(UUID userId, List<RitualHistoryStatus> statuses);
}
