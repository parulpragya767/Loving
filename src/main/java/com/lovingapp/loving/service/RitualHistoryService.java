package com.lovingapp.loving.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.loving.exception.ResourceNotFoundException;
import com.lovingapp.loving.mapper.RitualHistoryMapper;
import com.lovingapp.loving.model.dto.CurrentRitualDTOs.CurrentRitualDTO;
import com.lovingapp.loving.model.dto.CurrentRitualDTOs.CurrentRitualPackDTO;
import com.lovingapp.loving.model.dto.CurrentRitualDTOs.CurrentRitualsDTO;
import com.lovingapp.loving.model.dto.RitualDTO;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.RitualHistoryCreateRequest;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.RitualHistoryDTO;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.StatusUpdateEntry;
import com.lovingapp.loving.model.dto.RitualPackDTO;
import com.lovingapp.loving.model.entity.RitualHistory;
import com.lovingapp.loving.model.enums.RitualFeedback;
import com.lovingapp.loving.model.enums.RitualHistoryStatus;
import com.lovingapp.loving.repository.RitualHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RitualHistoryService {

	private final RitualHistoryRepository ritualHistoryRepository;
	private final RitualPackService ritualPackService;
	private final RitualService ritualService;

	public List<RitualHistoryDTO> listByUser(UUID userId) {
		return ritualHistoryRepository.findByUserIdOrderByUpdatedAtDesc(userId)
				.stream()
				.map(RitualHistoryMapper::toDto)
				.collect(Collectors.toList());
	}

	public List<RitualHistory> findByIds(UUID userId, List<UUID> ids) {
		Map<UUID, RitualHistory> historiesById = ritualHistoryRepository.findByIdInAndUserId(ids, userId)
				.stream()
				.collect(Collectors.toMap(RitualHistory::getId, Function.identity()));

		List<UUID> missingIds = ids.stream()
				.filter(id -> !historiesById.containsKey(id))
				.toList();

		if (!missingIds.isEmpty()) {
			throw new ResourceNotFoundException("RitualHistory", "ids", missingIds);
		}
		return historiesById.values().stream().collect(Collectors.toList());
	}

	public CurrentRitualsDTO listCurrentByUser(UUID userId) {
		// 1. Load all active histories
		List<RitualHistory> histories = ritualHistoryRepository.findByUserIdAndStatusInOrderByUpdatedAtDesc(userId,
				List.of(RitualHistoryStatus.ACTIVE, RitualHistoryStatus.STARTED));

		if (histories.isEmpty()) {
			return new CurrentRitualsDTO(List.of(), List.of());
		}

		// 2. Collect IDs for batch fetching rituals and ritual packs
		Set<UUID> ritualIds = histories.stream()
				.map(RitualHistory::getRitualId)
				.collect(Collectors.toSet());

		Set<UUID> packIds = histories.stream()
				.map(RitualHistory::getRitualPackId)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());

		// 3. Batch fetch rituals and ritual packs
		Map<UUID, RitualDTO> ritualMap = ritualService.findAllById(new ArrayList<>(ritualIds))
				.stream().collect(Collectors.toMap(RitualDTO::getId, Function.identity()));

		Map<UUID, RitualPackDTO> ritualPackMap = ritualPackService.findAllById(new ArrayList<>(packIds))
				.stream().collect(Collectors.toMap(RitualPackDTO::getId, Function.identity()));

		// 4. Group by recommendationId (pack instances)
		Map<UUID, List<RitualHistory>> groupedByRecommendation = histories.stream()
				.filter(h -> h.getRecommendationId() != null)
				.collect(Collectors.groupingBy(RitualHistory::getRecommendationId));

		// 5. Build pack DTOs
		List<CurrentRitualPackDTO> packDTOs = groupedByRecommendation.entrySet()
				.stream()
				.map(entry -> buildCurrentRitualPack(
						entry.getKey(),
						entry.getValue(),
						ritualPackMap,
						ritualMap))
				.collect(Collectors.toList());

		// 6. Build individual rituals
		List<CurrentRitualDTO> individualRituals = histories.stream()
				.filter(h -> h.getRecommendationId() == null)
				.map(h -> buildCurrentRitual(h, ritualMap.get(h.getRitualId())))
				.collect(Collectors.toList());

		// 7. Done
		CurrentRitualsDTO dto = new CurrentRitualsDTO();
		dto.setRitualPacks(packDTOs);
		dto.setIndividualRituals(individualRituals);
		return dto;
	}

	private CurrentRitualPackDTO buildCurrentRitualPack(
			UUID recommendationId,
			List<RitualHistory> histories,
			Map<UUID, RitualPackDTO> ritualPackMap,
			Map<UUID, RitualDTO> ritualMap) {
		// All histories in one recommendation have same packId
		UUID ritualPackId = histories.get(0).getRitualPackId();
		RitualPackDTO ritualPack = ritualPackMap.get(ritualPackId);

		List<CurrentRitualDTO> rituals = histories.stream()
				.map(h -> buildCurrentRitual(h, ritualMap.get(h.getRitualId())))
				.collect(Collectors.toList());

		CurrentRitualPackDTO dto = new CurrentRitualPackDTO();
		dto.setRitualPackId(ritualPackId);
		dto.setRecommendationId(recommendationId);
		dto.setRitualPack(ritualPack);
		dto.setRituals(rituals);
		return dto;
	}

	private CurrentRitualDTO buildCurrentRitual(RitualHistory history, RitualDTO ritual) {
		CurrentRitualDTO dto = new CurrentRitualDTO();
		dto.setRitualId(history.getRitualId());
		dto.setRitualHistoryId(history.getId());
		dto.setStatus(history.getStatus());
		dto.setRitual(ritual);
		return dto;
	}

	@Transactional
	public RitualHistoryDTO create(UUID userId, RitualHistoryCreateRequest request) {
		// Validate the ritual ID and ritual pack ID in request
		validateRitualHistoriesCreateRequests(List.of(request));

		RitualHistory ritualHistory = RitualHistory.builder()
				.userId(userId)
				.ritualId(request.getRitualId())
				.ritualPackId(request.getRitualPackId())
				.recommendationId(request.getRecommendationId())
				.status(request.getStatus())
				.build();

		RitualHistory saved = ritualHistoryRepository.saveAndFlush(ritualHistory);
		return RitualHistoryMapper.toDto(saved);
	}

	@Transactional
	public void delete(UUID ritualHistoryId) {
		ritualHistoryRepository.findById(ritualHistoryId)
				.orElseThrow(() -> new ResourceNotFoundException("RitualHistory", "id", ritualHistoryId));

		ritualHistoryRepository.deleteById(ritualHistoryId);
	}

	@Transactional
	public List<RitualHistoryDTO> bulkCreateRitualHistories(UUID userId, List<RitualHistoryCreateRequest> requests) {
		if (requests == null || requests.isEmpty()) {
			return List.of();
		}

		// Validate all ritual IDs and ritual pack IDs in bulk
		validateRitualHistoriesCreateRequests(requests);

		List<RitualHistory> histories = requests.stream()
				.map(request -> RitualHistory.builder()
						.userId(userId)
						.ritualId(request.getRitualId())
						.ritualPackId(request.getRitualPackId())
						.recommendationId(request.getRecommendationId())
						.status(request.getStatus())
						.build())
				.collect(Collectors.toList());

		// Save all in a single batch
		List<RitualHistory> savedHistories = ritualHistoryRepository.saveAllAndFlush(histories);

		// Convert back to DTOs and return
		List<RitualHistoryDTO> result = savedHistories.stream()
				.map(RitualHistoryMapper::toDto)
				.collect(Collectors.toList());
		return result;
	}

	@Transactional
	public void updateStatus(UUID ritualHistoryId, UUID userId, RitualHistoryStatus status,
			RitualFeedback feedback) {

		RitualHistory ritualHistory = ritualHistoryRepository.findByIdAndUserId(ritualHistoryId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("RitualHistory", "id", ritualHistoryId));

		ritualHistory.setStatus(status);
		if (feedback != null) {
			ritualHistory.setFeedback(feedback);
		}
		ritualHistoryRepository.save(ritualHistory);
	}

	@Transactional
	public void bulkUpdateStatus(UUID userId, List<StatusUpdateEntry> updates) {
		if (updates == null || updates.isEmpty()) {
			return;
		}

		List<UUID> ids = updates.stream()
				.map(StatusUpdateEntry::getRitualHistoryId)
				.toList();

		// Validate that all ritual history IDs exist
		Map<UUID, RitualHistory> historiesById = findByIds(userId, ids).stream()
				.collect(Collectors.toMap(RitualHistory::getId, Function.identity()));

		// Create a map of updates by ritual history ID for quick lookup
		Map<UUID, StatusUpdateEntry> updatesMap = updates.stream()
				.collect(Collectors.toMap(StatusUpdateEntry::getRitualHistoryId, Function.identity()));

		// Update each history with its corresponding status and feedback
		for (Map.Entry<UUID, RitualHistory> entry : historiesById.entrySet()) {
			UUID historyId = entry.getKey();
			RitualHistory history = entry.getValue();
			StatusUpdateEntry update = updatesMap.get(historyId);

			if (update != null) {
				history.setStatus(update.getStatus());
			}
		}

		ritualHistoryRepository.saveAll(historiesById.values());
	}

	private void validateRitualHistoriesCreateRequests(List<RitualHistoryCreateRequest> requests) {
		// Collect all unique ritual IDs
		List<UUID> ritualIds = requests.stream()
				.map(RitualHistoryCreateRequest::getRitualId)
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toList());

		// Validate all ritual IDs exist in bulk
		ritualService.findAllById(ritualIds);

		// Collect all unique ritual pack IDs
		List<UUID> ritualPackIds = requests.stream()
				.map(RitualHistoryCreateRequest::getRitualPackId)
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toList());

		if (!ritualPackIds.isEmpty()) {
			// Validate all ritual pack IDs exist in bulk
			Map<UUID, RitualPackDTO> ritualPackMap = ritualPackService.findAllById(ritualPackIds)
					.stream()
					.collect(Collectors.toMap(RitualPackDTO::getId, Function.identity()));

			// Validate that all rituals are associated with their respective packs
			Map<UUID, UUID> invalidPairs = new HashMap<>(); // ritualId -> ritualPackId

			for (RitualHistoryCreateRequest request : requests) {
				if (request.getRitualPackId() != null) {
					RitualPackDTO ritualPack = ritualPackMap.get(request.getRitualPackId());
					if (ritualPack != null && !ritualPack.getRitualIds().contains(request.getRitualId())) {
						invalidPairs.put(request.getRitualId(), request.getRitualPackId());
					}
				}
			}

			// If there are any errors, throw an exception with all of them
			if (!invalidPairs.isEmpty()) {
				throw new IllegalArgumentException(
						"Ritual not associated with ritual pack (ritualId -> ritualPackId): " + String.join(", ",
								invalidPairs.entrySet().stream()
										.map(entry -> entry.getKey() + " -> " + entry.getValue())
										.collect(Collectors.toList())));
			}
		}
	}
}
