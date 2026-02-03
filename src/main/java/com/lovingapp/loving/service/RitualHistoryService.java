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
import com.lovingapp.loving.model.dto.RitualDTO;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.RitualHistoryCreateRequest;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.RitualHistoryDTO;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.StatusUpdateEntry;
import com.lovingapp.loving.model.dto.RitualPackDTO;
import com.lovingapp.loving.model.dto.UserRitualsDTOs.CurrentRitualsDTO;
import com.lovingapp.loving.model.dto.UserRitualsDTOs.UserRitualDTO;
import com.lovingapp.loving.model.dto.UserRitualsDTOs.UserRitualPackDTO;
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

	public List<UserRitualDTO> listByUser(UUID userId, RitualHistoryStatus status) {
		List<RitualHistory> histories = ritualHistoryRepository.findByUserIdOrderByUpdatedAtDesc(userId);

		if (status != null) {
			histories = histories.stream()
					.filter(h -> h.getStatus() == status)
					.collect(Collectors.toList());
		}

		if (histories.isEmpty()) {
			return List.of();
		}

		RitualHistoryBuildContext ctx = buildContext(histories);
		return histories.stream()
				.map(history -> buildUserRitual(history, ctx.ritualMap().get(history.getRitualId())))
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
		// Load all active histories
		List<RitualHistory> histories = ritualHistoryRepository.findByUserIdAndStatusInOrderByUpdatedAtDesc(userId,
				List.of(RitualHistoryStatus.ACTIVE, RitualHistoryStatus.STARTED));

		if (histories.isEmpty()) {
			return new CurrentRitualsDTO(List.of(), List.of());
		}

		// Fetch the rituals and ritual pack context
		RitualHistoryBuildContext ctx = buildContext(histories);

		// Build pack DTOs
		List<UserRitualPackDTO> packDTOs = ctx.historiesByRecommendationId().entrySet()
				.stream()
				.map(entry -> {
					List<RitualHistory> ritualHistories = entry.getValue();
					// All histories in one recommendation have same packId
					UUID ritualPackId = ritualHistories.get(0).getRitualPackId();
					RitualPackDTO ritualPack = ctx.ritualPackMap().get(ritualPackId);
					UserRitualPackDTO dto = buildUserRitualPack(
							entry.getKey(),
							histories,
							ritualPack,
							ctx.ritualMap());
					return dto;
				})
				.collect(Collectors.toList());

		// Build individual rituals
		List<UserRitualDTO> individualRituals = histories.stream()
				.filter(history -> history.getRecommendationId() == null)
				.map(history -> buildUserRitual(history, ctx.ritualMap().get(history.getRitualId())))
				.collect(Collectors.toList());

		CurrentRitualsDTO dto = CurrentRitualsDTO.builder()
				.ritualPacks(packDTOs)
				.individualRituals(individualRituals)
				.build();
		return dto;
	}

	public UserRitualPackDTO listByRecommendationId(UUID userId, UUID recommendationId) {
		List<RitualHistory> histories = ritualHistoryRepository
				.findByUserIdAndRecommendationIdOrderByUpdatedAtDesc(userId, recommendationId);
		if (histories.isEmpty()) {
			throw new ResourceNotFoundException("RitualHistory", "recommendationId", recommendationId);
		}

		RitualHistoryBuildContext ctx = buildContext(histories);

		// All histories in one recommendation have same packId
		UUID ritualPackId = histories.get(0).getRitualPackId();
		RitualPackDTO ritualPack = ctx.ritualPackMap().get(ritualPackId);
		return buildUserRitualPack(recommendationId, histories, ritualPack, ctx.ritualMap());
	}

	private record RitualHistoryBuildContext(
			Map<UUID, RitualDTO> ritualMap,
			Map<UUID, RitualPackDTO> ritualPackMap,
			Map<UUID, List<RitualHistory>> historiesByRecommendationId) {
	}

	private RitualHistoryBuildContext buildContext(List<RitualHistory> histories) {
		// Collect IDs for batch fetching rituals and ritual packs
		Set<UUID> ritualIds = histories.stream()
				.map(RitualHistory::getRitualId)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());

		Set<UUID> packIds = histories.stream()
				.map(RitualHistory::getRitualPackId)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());

		// Batch fetch rituals and ritual packs
		Map<UUID, RitualDTO> ritualMap = ritualService.findAllById(new ArrayList<>(ritualIds))
				.stream().collect(Collectors.toMap(RitualDTO::getId, Function.identity()));

		Map<UUID, RitualPackDTO> ritualPackMap = ritualPackService.findAllById(new ArrayList<>(packIds))
				.stream().collect(Collectors.toMap(RitualPackDTO::getId, Function.identity()));

		// Group by recommendationId (pack instances)
		Map<UUID, List<RitualHistory>> groupedByRecommendation = histories.stream()
				.filter(history -> history.getRecommendationId() != null)
				.collect(Collectors.groupingBy(RitualHistory::getRecommendationId));

		return new RitualHistoryBuildContext(ritualMap, ritualPackMap, groupedByRecommendation);
	}

	private UserRitualPackDTO buildUserRitualPack(
			UUID recommendationId,
			List<RitualHistory> histories,
			RitualPackDTO ritualPack,
			Map<UUID, RitualDTO> ritualMap) {
		List<UserRitualDTO> rituals = histories.stream()
				.map(history -> buildUserRitual(history, ritualMap.get(history.getRitualId())))
				.collect(Collectors.toList());

		UserRitualPackDTO dto = UserRitualPackDTO.builder()
				.ritualPackId(ritualPack.getId())
				.recommendationId(recommendationId)
				.ritualPack(ritualPack)
				.rituals(rituals)
				.build();
		return dto;
	}

	private UserRitualDTO buildUserRitual(RitualHistory history, RitualDTO ritual) {
		UserRitualDTO dto = UserRitualDTO.builder()
				.ritualId(history.getRitualId())
				.ritualHistoryId(history.getId())
				.ritualHistory(RitualHistoryMapper.toDto(history))
				.ritual(ritual)
				.build();
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
