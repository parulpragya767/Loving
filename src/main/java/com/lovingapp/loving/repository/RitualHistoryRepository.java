package com.lovingapp.loving.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lovingapp.loving.model.RitualHistory;

public interface RitualHistoryRepository extends JpaRepository<RitualHistory, UUID> {
    List<RitualHistory> findByUserIdOrderByOccurredAtDesc(UUID userId);

    List<RitualHistory> findByRitualIdOrderByOccurredAtDesc(UUID ritualId);

    List<RitualHistory> findByUserIdAndRitualIdOrderByOccurredAtDesc(UUID userId, UUID ritualId);
}
