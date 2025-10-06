package com.lovingapp.loving.repository;

import com.lovingapp.loving.model.RitualHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RitualHistoryRepository extends JpaRepository<RitualHistory, UUID> {
    List<RitualHistory> findByUserIdOrderByOccurredAtDesc(UUID userId);
    List<RitualHistory> findByRitualIdOrderByOccurredAtDesc(UUID ritualId);
    List<RitualHistory> findByUserIdAndRitualIdOrderByOccurredAtDesc(UUID userId, UUID ritualId);
}
