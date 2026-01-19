package com.lovingapp.loving.service.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.lovingapp.loving.model.dto.RitualHistoryDTOs.RitualHistoryCreateRequest;
import com.lovingapp.loving.model.dto.RitualHistoryDTOs.RitualHistoryDTO;
import com.lovingapp.loving.model.dto.RitualPackDTO;
import com.lovingapp.loving.model.dto.RitualRecommendationDTOs.RitualRecommendationCreateRequest;
import com.lovingapp.loving.model.dto.RitualRecommendationDTOs.RitualRecommendationDTO;
import com.lovingapp.loving.model.enums.RecommendationSource;
import com.lovingapp.loving.model.enums.RecommendationStatus;
import com.lovingapp.loving.model.enums.RitualHistoryStatus;
import com.lovingapp.loving.service.RitualHistoryService;
import com.lovingapp.loving.service.RitualRecommendationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Helper service for managing ritual recommendations and history creation in AI
 * chat context.
 * Handles the creation of ritual recommendations and corresponding history
 * records.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatRitualRecommendationAndHistoryHelper {

    private final RitualRecommendationService ritualRecommendationService;
    private final RitualHistoryService ritualHistoryService;
    private final AIChatMessagePersistenceService chatMessagePersistenceService;

    /**
     * Create ritual recommendation and history records if pack was recommended.
     */
    public List<RitualHistoryDTO> createRecommendationAndHistory(UUID userId, UUID sessionId,
            RitualPackDTO recommendedPack) {
        List<RitualHistoryDTO> createdHistories = new ArrayList<>();

        if (recommendedPack != null) {
            // Create ritual recommendation record
            RitualRecommendationCreateRequest recommendationCreateRequest = RitualRecommendationCreateRequest.builder()
                    .source(RecommendationSource.CHAT)
                    .sourceId(sessionId)
                    .ritualPackId(recommendedPack.getId())
                    .status(RecommendationStatus.SUGGESTED)
                    .build();
            RitualRecommendationDTO savedRecommendation = ritualRecommendationService.create(userId,
                    recommendationCreateRequest);

            log.info("Ritual recommendation saved successfully sessionId={} recommendationId={}", sessionId,
                    savedRecommendation.getId());

            // Create and save system chat message with recommendation metadata
            chatMessagePersistenceService.saveRecommendationMessage(sessionId, savedRecommendation.getId());

            // Bulk create ritual history records for the rituals inside recommended ritual
            // pack
            createdHistories = createRitualHistories(userId, sessionId, recommendedPack,
                    savedRecommendation.getId());
        }

        return createdHistories;
    }

    /**
     * Create ritual history records for the rituals inside the recommended ritual
     * pack.
     */
    public List<RitualHistoryDTO> createRitualHistories(UUID userId, UUID sessionId, RitualPackDTO recommendedPack,
            UUID recommendationId) {
        List<UUID> ritualIds = null;
        if (recommendedPack.getRituals() != null && !recommendedPack.getRituals().isEmpty()) {
            ritualIds = recommendedPack.getRituals().stream()
                    .map(r -> r.getId())
                    .collect(Collectors.toList());
        } else if (recommendedPack.getRitualIds() != null && !recommendedPack.getRitualIds().isEmpty()) {
            ritualIds = recommendedPack.getRitualIds();
        }

        List<RitualHistoryDTO> createdHistories = new ArrayList<>();
        if (ritualIds != null && !ritualIds.isEmpty()) {
            UUID packId = recommendedPack.getId();
            List<RitualHistoryCreateRequest> histories = ritualIds.stream()
                    .map(ritualId -> RitualHistoryCreateRequest.builder()
                            .ritualId(ritualId)
                            .ritualPackId(packId)
                            .recommendationId(recommendationId)
                            .status(RitualHistoryStatus.SUGGESTED)
                            .build())
                    .collect(Collectors.toList());
            createdHistories = ritualHistoryService.bulkCreateRitualHistories(userId, histories);

            log.info(
                    "Ritual history records created for recommended pack sessionId={} recommendationId={} count={}",
                    sessionId, recommendationId, createdHistories.size());
        }

        return createdHistories;
    }
}
