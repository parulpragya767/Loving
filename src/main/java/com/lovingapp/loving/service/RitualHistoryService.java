package com.lovingapp.loving.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

	public RitualHistoryDTO findById(UUID id) {
		return ritualHistoryRepository.findById(id)
				.map(RitualHistoryMapper::toDto)
				.orElseThrow(() -> new ResourceNotFoundException("RitualHistory", "id", id));
	}

	public List<RitualHistoryDTO> findByUserAndRecommendationId(UUID userId, UUID recommendationId) {
		return ritualHistoryRepository.findByUserIdAndRecommendationId(userId, recommendationId)
				.stream()
				.map(RitualHistoryMapper::toDto)
				.collect(Collectors.toList());
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
		if (!ritualService.existsById(request.getRitualId())) {
			throw new ResourceNotFoundException("Ritual", "id", request.getRitualId());
		}

		if (request.getRitualPackId() != null) {
			RitualPackDTO ritualPack = ritualPackService.findById(request.getRitualPackId());

			if (!ritualPack.getRitualIds().contains(request.getRitualId())) {
				throw new IllegalArgumentException(
						String.format("Ritual not associated with ritual pack: ritualId=%s ritualPackId=%s",
								request.getRitualId(),
								request.getRitualPackId()));
			}
		}

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
		log.info("Deleting ritual history entry ritualHistoryId={}", ritualHistoryId);
		ritualHistoryRepository.deleteById(ritualHistoryId);
	}

	@Transactional
	public List<RitualHistoryDTO> bulkCreateRitualHistories(UUID userId,
			List<RitualHistoryCreateRequest> requests) {
		if (requests == null || requests.isEmpty()) {
			return List.of();
		}
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
	public RitualHistoryDTO updateStatus(UUID ritualHistoryId, UUID userId, RitualHistoryStatus status,
			RitualFeedback feedback) {
		log.info("Updating ritual history status ritualHistoryId={} status={}", ritualHistoryId, status);
		if (feedback != null) {
			log.debug("Ritual feedback received ritualHistoryId={} feedback={}", ritualHistoryId, feedback);
		}
		RitualHistory ritualHistory = ritualHistoryRepository.findById(ritualHistoryId)
				.filter(history -> history.getUserId().equals(userId))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"Ritual history not found with id: " + ritualHistoryId));

		ritualHistory.setStatus(status);
		if (feedback != null) {
			ritualHistory.setFeedback(feedback);
		}
		RitualHistory saved = ritualHistoryRepository.save(ritualHistory);
		log.info("Ritual history status updated successfully ritualHistoryId={}", saved.getId());
		return RitualHistoryMapper.toDto(saved);
	}

	@Transactional
	public List<RitualHistoryDTO> bulkUpdateStatus(UUID userId, List<StatusUpdateEntry> updates) {
		log.info("Bulk updating ritual history status count={}", updates == null ? 0 : updates.size());
		log.debug("Bulk status updates payload: {}", updates);
		if (updates == null || updates.isEmpty()) {
			return List.of();
		}

		// Get all ritual history IDs from the updates
		List<UUID> ritualHistoryIds = updates.stream()
				.map(StatusUpdateEntry::getRitualHistoryId)
				.collect(Collectors.toList());

		// Find all ritual histories that belong to the user
		Map<UUID, RitualHistory> historyMap = ritualHistoryRepository.findAllById(ritualHistoryIds).stream()
				.filter(rh -> rh.getUserId().equals(userId))
				.collect(Collectors.toMap(RitualHistory::getId, rh -> rh));

		// Create a map of updates by ritual history ID for quick lookup
		Map<UUID, StatusUpdateEntry> updatesMap = updates.stream()
				.collect(Collectors.toMap(
						StatusUpdateEntry::getRitualHistoryId,
						update -> update));

		// Update each history with its corresponding status and feedback
		List<RitualHistory> historiesToUpdate = new ArrayList<>();
		for (Map.Entry<UUID, RitualHistory> entry : historyMap.entrySet()) {
			UUID historyId = entry.getKey();
			RitualHistory history = entry.getValue();
			StatusUpdateEntry update = updatesMap.get(historyId);

			if (update != null) {
				history.setStatus(update.getStatus());
				historiesToUpdate.add(history);
			}
		}

		// Save all updates
		List<RitualHistory> savedHistories = ritualHistoryRepository.saveAll(historiesToUpdate);

		// Convert to DTOs and return
		List<RitualHistoryDTO> result = savedHistories.stream()
				.map(RitualHistoryMapper::toDto)
				.collect(Collectors.toList());
		log.info("Bulk ritual history status update completed successfully count={}", result.size());
		return result;
	}
}
