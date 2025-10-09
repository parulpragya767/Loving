package com.lovingapp.loving.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.loving.model.RitualHistory;
import com.lovingapp.loving.repository.RitualHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RitualHistoryService {

    private final RitualHistoryRepository ritualHistoryRepository;

    public List<RitualHistory> listByUser(UUID userId) {
        return ritualHistoryRepository.findByUserIdOrderByOccurredAtDesc(userId);
    }

    public List<RitualHistory> listByUserAndRitual(UUID userId, UUID ritualId) {
        return ritualHistoryRepository.findByUserIdAndRitualIdOrderByOccurredAtDesc(userId, ritualId);
    }

    public Optional<RitualHistory> findById(UUID id) {
        return ritualHistoryRepository.findById(id);
    }

    @Transactional
    public RitualHistory save(RitualHistory entity) {
        return ritualHistoryRepository.save(entity);
    }

    @Transactional
    public void delete(RitualHistory entity) {
        ritualHistoryRepository.delete(entity);
    }
}
