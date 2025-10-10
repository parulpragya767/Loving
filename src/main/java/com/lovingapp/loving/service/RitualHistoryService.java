package com.lovingapp.loving.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.loving.mapper.RitualHistoryMapper;
import com.lovingapp.loving.model.dto.RitualHistoryDTO;
import com.lovingapp.loving.model.entity.RitualHistory;
import com.lovingapp.loving.model.enums.EmojiFeedback;
import com.lovingapp.loving.model.enums.RitualHistoryStatus;
import com.lovingapp.loving.repository.RitualHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RitualHistoryService {

    private final RitualHistoryRepository ritualHistoryRepository;

    public List<RitualHistoryDTO> listByUser(UUID userId) {
        return ritualHistoryRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(RitualHistoryMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<RitualHistoryDTO> listActiveByUser(UUID userId) {
        return ritualHistoryRepository.findByUserIdAndStatusInOrderByUpdatedAtDesc(
                userId,
                List.of(RitualHistoryStatus.SUGGESTED, RitualHistoryStatus.STARTED))
                .stream()
                .map(RitualHistoryMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<RitualHistoryDTO> findById(UUID id) {
        return ritualHistoryRepository.findById(id)
                .map(RitualHistoryMapper::toDto);
    }

    @Transactional
    public RitualHistoryDTO save(RitualHistoryDTO entity) {
        RitualHistory ritualHistory = RitualHistoryMapper.fromDto(entity);
        RitualHistory saved = ritualHistoryRepository.save(ritualHistory);
        return RitualHistoryMapper.toDto(saved);
    }

    @Transactional
    public void delete(RitualHistoryDTO entity) {
        RitualHistory ritualHistory = RitualHistoryMapper.fromDto(entity);
        ritualHistoryRepository.delete(ritualHistory);
    }

    @Transactional
    public Optional<RitualHistoryDTO> updateStatusWithOwnership(UUID id, UUID userId, RitualHistoryStatus status,
            EmojiFeedback feedback) {
        Optional<RitualHistory> found = ritualHistoryRepository.findById(id)
                .filter(rh -> rh.getUserId().equals(userId));
        if (found.isEmpty()) {
            return Optional.empty();
        }
        RitualHistory rh = found.get();
        rh.setStatus(status);
        if (feedback != null) {
            rh.setFeedback(feedback);
        }
        RitualHistory saved = ritualHistoryRepository.save(rh);
        return Optional.ofNullable(RitualHistoryMapper.toDto(saved));
    }
}
