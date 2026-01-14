package com.lovingapp.loving.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.loving.exception.ResourceNotFoundException;
import com.lovingapp.loving.mapper.RitualRecommendationMapper;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.StatusUpdateEntry;
import com.lovingapp.loving.model.dto.RitualRecommendationDTOs.RitualRecommendationCreateRequest;
import com.lovingapp.loving.model.dto.RitualRecommendationDTOs.RitualRecommendationDTO;
import com.lovingapp.loving.model.dto.RitualRecommendationDTOs.RitualRecommendationUpdateRequest;
import com.lovingapp.loving.model.entity.RitualRecommendation;
import com.lovingapp.loving.repository.RitualRecommendationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RitualRecommendationService {

    private final RitualRecommendationRepository ritualRecommendationRepository;
    private final RitualHistoryService ritualHistoryService;

    @Transactional(readOnly = true)
    public List<RitualRecommendationDTO> getAll(UUID userId) {
        return ritualRecommendationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(RitualRecommendationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RitualRecommendationDTO getById(UUID userId, UUID id) {
        return ritualRecommendationRepository.findByIdAndUserId(id, userId)
                .map(RitualRecommendationMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("RitualRecommendation", "id", id));
    }

    @Transactional
    public RitualRecommendationDTO create(UUID userId, RitualRecommendationCreateRequest request) {
        RitualRecommendation entity = RitualRecommendation.builder()
                .userId(userId)
                .source(request.getSource())
                .sourceId(request.getSourceId())
                .ritualPackId(request.getRitualPackId())
                .status(request.getStatus())
                .build();

        RitualRecommendation saved = ritualRecommendationRepository.saveAndFlush(entity);
        return RitualRecommendationMapper.toDto(saved);
    }

    @Transactional
    public void updateRecommendationAndRitualHistoryStatus(UUID userId, UUID recommendationId,
            RitualRecommendationUpdateRequest request) {
        RitualRecommendation ritualRecommendation = ritualRecommendationRepository
                .findByIdAndUserId(recommendationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("RitualRecommendation", "id", recommendationId));

        ritualRecommendation.setStatus(request.getStatus());
        ritualRecommendationRepository.save(ritualRecommendation);

        log.info("Ritual recommendation status updated successfully recommendationId={} status={}", recommendationId,
                request.getStatus());

        if (request.getRitualStatusUpdates() != null && !request.getRitualStatusUpdates().isEmpty()) {
            List<StatusUpdateEntry> ritualHistoryStatusUpdates = request.getRitualStatusUpdates().stream()
                    .map(update -> StatusUpdateEntry.builder()
                            .ritualHistoryId(update.getRitualId())
                            .status(update.getStatus())
                            .build())
                    .collect(Collectors.toList());

            // Bulk update all related ritual histories
            ritualHistoryService.bulkUpdateStatus(userId, ritualHistoryStatusUpdates);

            log.info("Ritual history status updated successfully.");
        }
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        RitualRecommendation entity = ritualRecommendationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("RitualRecommendation", "id", id));
        ritualRecommendationRepository.delete(entity);
    }
}
