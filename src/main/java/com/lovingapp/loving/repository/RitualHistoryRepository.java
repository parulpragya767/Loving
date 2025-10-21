package com.lovingapp.loving.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lovingapp.loving.model.entity.RitualHistory;
import com.lovingapp.loving.model.enums.RitualHistoryStatus;

public interface RitualHistoryRepository extends JpaRepository<RitualHistory, UUID> {
    List<RitualHistory> findByUserIdOrderByUpdatedAtDesc(UUID userId);

    List<RitualHistory> findByUserIdAndStatusInOrderByUpdatedAtDesc(UUID userId, List<RitualHistoryStatus> statuses);
    
    List<RitualHistory> findByUserIdAndRitualPackId(UUID userId, UUID ritualPackId);
}
