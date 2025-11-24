package com.lovingapp.loving.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.lovingapp.loving.exception.ResourceNotFoundException;
import com.lovingapp.loving.mapper.RitualRecommendationMapper;
import com.lovingapp.loving.model.dto.RitualRecommendationDTOs.RitualRecommendationDTO;
import com.lovingapp.loving.model.entity.RitualRecommendation;
import com.lovingapp.loving.model.enums.RecommendationStatus;
import com.lovingapp.loving.repository.RitualRecommendationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RitualRecommendationService {

    private final RitualRecommendationRepository ritualRecommendationRepository;

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
    public RitualRecommendationDTO updateStatus(UUID userId, UUID recommendationId, RecommendationStatus status) {
        RitualRecommendation ritualRecommendation = ritualRecommendationRepository.findById(recommendationId)
                .filter(history -> history.getUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Ritual recommendation not found with id: " + recommendationId));

        ritualRecommendation.setStatus(status);
        RitualRecommendation saved = ritualRecommendationRepository.save(ritualRecommendation);
        return RitualRecommendationMapper.toDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        RitualRecommendation entity = ritualRecommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RitualRecommendation not found with id: " + id));
        ritualRecommendationRepository.delete(entity);
    }
}
