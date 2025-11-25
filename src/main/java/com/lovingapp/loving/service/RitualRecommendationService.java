package com.lovingapp.loving.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.lovingapp.loving.exception.ResourceNotFoundException;
import com.lovingapp.loving.mapper.RitualRecommendationMapper;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.RitualHistoryDTO;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.StatusUpdateEntry;
import com.lovingapp.loving.model.dto.RitualRecommendationDTOs.RitualRecommendationDTO;
import com.lovingapp.loving.model.dto.RitualRecommendationDTOs.RitualRecommendationUpdateRequest;
import com.lovingapp.loving.model.dto.RitualRecommendationDTOs.RitualStatusUpdate;
import com.lovingapp.loving.model.entity.RitualRecommendation;
import com.lovingapp.loving.model.enums.RitualHistoryStatus;
import com.lovingapp.loving.repository.RitualRecommendationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
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
    public RitualRecommendationDTO getById(UUID id) {
        return ritualRecommendationRepository.findById(id)
                .map(RitualRecommendationMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("RitualRecommendation not found with id: " + id));
    }

    @Transactional
    public RitualRecommendationDTO create(UUID userId, RitualRecommendationDTO dto) {
        RitualRecommendation entity = RitualRecommendationMapper.fromDto(dto);
        entity.setUserId(userId);
        RitualRecommendation saved = ritualRecommendationRepository.saveAndFlush(entity);
        return RitualRecommendationMapper.toDto(saved);
    }

    @Transactional
    public void updateRecommendationAndRitualHistoryStatus(UUID userId, UUID recommendationId,
            RitualRecommendationUpdateRequest request) {
        RitualRecommendation ritualRecommendation = ritualRecommendationRepository.findById(recommendationId)
                .filter(recommendation -> recommendation.getUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Ritual recommendation not found with id: " + recommendationId));

        ritualRecommendation.setStatus(request.getStatus());
        ritualRecommendationRepository.save(ritualRecommendation);

        if (request.getRitualStatusUpdates() != null && !request.getRitualStatusUpdates().isEmpty()) {
            List<RitualHistoryDTO> ritualHistories = ritualHistoryService.findByUserAndRecommendationId(userId,
                    recommendationId);

            if (!ritualHistories.isEmpty()) {
                Map<UUID, RitualHistoryStatus> statusUpdateMap = request.getRitualStatusUpdates().stream()
                        .collect(Collectors.toMap(
                                RitualStatusUpdate::getRitualId,
                                RitualStatusUpdate::getStatus));

                // Create update entries, looking up the status for each ritual
                List<StatusUpdateEntry> updates = ritualHistories.stream()
                        .filter(update -> statusUpdateMap.containsKey(update.getRitualId()))
                        .map(history -> StatusUpdateEntry.builder()
                                .ritualHistoryId(history.getId())
                                .status(statusUpdateMap.get(history.getRitualId()))
                                .build())
                        .collect(Collectors.toList());

                // Bulk update all related ritual histories
                ritualHistoryService.bulkUpdateStatus(userId, updates);
            }
        }
    }

    @Transactional
    public void delete(UUID id) {
        RitualRecommendation entity = ritualRecommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RitualRecommendation not found with id: " + id));
        ritualRecommendationRepository.delete(entity);
    }
}
