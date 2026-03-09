package com.lovingapp.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.model.domain.RitualRecommendationContext;
import com.lovingapp.model.dto.RitualPackDTO;
import com.lovingapp.model.enums.Journey;
import com.lovingapp.model.enums.LoveType;
import com.lovingapp.model.enums.RelationalNeed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationEngine {

    private final RitualPackService ritualPackService;

    /**
     * Recommends ritual packs based on the recommendation context.
     * Uses a sophisticated scoring system with journey filtering, weighted love
     * type scoring,
     * relational need intersection, history penalty, and recency penalty.
     * 
     * @param context The recommendation context containing preferences, needs, and
     *                history
     * @param limit   The maximum number of ritual packs to return
     * @return A list of recommended RitualPackDTOs, ordered by relevance score
     *         (0-1)
     */
    @Transactional(readOnly = true)
    public List<RitualPackDTO> recommend(RitualRecommendationContext context, int limit) {
        List<RitualPackDTO> allPacks = ritualPackService.findAll();

        if (allPacks.isEmpty()) {
            log.info("No ritual packs available for recommendation");
            return List.of();
        }

        if (context == null) {
            log.info("Cannot score ritual packs: context is null (returning first {} packs)", limit);
            return allPacks.stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        // Filter by journey type, fallback to all packs if no journey or empty
        List<RitualPackDTO> filteredPacks = filterByJourneyType(allPacks, context.getJourney());

        // Calculate recommendation counts for history penalty
        Map<UUID, Long> recommendationCounts = calculateRecommendationCounts(context);

        // Get last recommended pack ID for recency penalty
        UUID lastRecommendedPackId = getLastRecommendedPackId(context);

        // Score and sort ritual packs based on context
        List<RitualPackDTO> result = filteredPacks.stream()
                .map(pack -> {
                    float score = calculateMatchScore(pack, context, recommendationCounts, lastRecommendedPackId);
                    return new ScoredPack(pack, score);
                })
                .sorted(Comparator.comparingDouble(ScoredPack::getScore).reversed())
                .limit(limit)
                .map(ScoredPack::getPack)
                .collect(Collectors.toList());

        log.info("Recommended {} ritual packs from {} filtered candidates", result.size(), filteredPacks.size());
        return result;
    }

    /**
     * Filters ritual packs by journey type. If journey is null or no matches found,
     * returns all packs as fallback.
     */
    private List<RitualPackDTO> filterByJourneyType(List<RitualPackDTO> allPacks, Journey journey) {
        if (journey == null) {
            log.info("No journey type specified, using all packs");
            return allPacks;
        }

        List<RitualPackDTO> filtered = allPacks.stream()
                .filter(pack -> pack.getJourney() != null && pack.getJourney().equals(journey))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            log.info("No packs found for journey type {}, falling back to all packs", journey);
            return allPacks;
        }
        return filtered;
    }

    /**
     * Calculates how many times a pack has already been recommended from history
     * for penalty calculation.
     */
    private Map<UUID, Long> calculateRecommendationCounts(RitualRecommendationContext context) {
        if (context.getRecommendationHistory() == null) {
            return Map.of();
        }

        return context.getRecommendationHistory().stream()
                .filter(event -> event != null && event.getRitualPackId() != null)
                .collect(Collectors.groupingBy(
                        event -> event.getRitualPackId(),
                        Collectors.counting()));
    }

    /**
     * Gets the last recommended pack ID for recency penalty.
     */
    private UUID getLastRecommendedPackId(RitualRecommendationContext context) {
        if (context.getRecommendationHistory() == null || context.getRecommendationHistory().isEmpty()) {
            return null;
        }

        return context.getRecommendationHistory().stream()
                .filter(event -> event != null && event.getRitualPackId() != null)
                .max(Comparator.comparing(event -> event.getRecommendedAt()))
                .map(event -> event.getRitualPackId())
                .orElse(null);
    }

    /**
     * Calculates a comprehensive match score for a ritual pack based on
     * recommendation context.
     * Score is normalized between 0 and 1, with higher scores indicating better
     * matches.
     */
    private float calculateMatchScore(RitualPackDTO pack, RitualRecommendationContext context,
            Map<UUID, Long> recommendationCounts, UUID lastRecommendedPackId) {

        // Calculate component scores
        float loveTypeScore = calculateLoveTypeScore(pack, context);
        float relationalNeedScore = calculateRelationalNeedScore(pack, context);

        // Combine base scores (equal weight for now)
        float baseScore = (loveTypeScore + relationalNeedScore) / 2.0f;

        // Apply penalties
        float historyPenalty = calculateHistoryPenalty(pack.getId(), recommendationCounts);
        float recencyPenalty = calculateRecencyPenalty(pack.getId(), lastRecommendedPackId);

        // Calculate final score
        float finalScore = baseScore * historyPenalty * recencyPenalty;

        // Ensure score is between 0 and 1
        finalScore = Math.max(0.0f, Math.min(1.0f, finalScore));

        // Debug logging
        log.debug("Pack {} ({}): loveTypeScore={:.3f}, relationalNeedScore={:.3f}, " +
                "baseScore={:.3f}, historyPenalty={:.3f}, recencyPenalty={:.3f}, finalScore={:.3f}",
                pack.getId(), pack.getTitle(), loveTypeScore, relationalNeedScore,
                baseScore, historyPenalty, recencyPenalty, finalScore);

        return finalScore;
    }

    /**
     * Calculates love type score with weighted matching.
     * First 2 love types get higher weight, remaining get lower weight.
     * Score is normalized between 0-1.
     */
    private float calculateLoveTypeScore(RitualPackDTO pack, RitualRecommendationContext context) {
        if (context.getLoveTypes() == null || context.getLoveTypes().isEmpty() ||
                pack.getLoveTypes() == null || pack.getLoveTypes().isEmpty()) {
            return 0.0f;
        }

        float totalWeight = 0.0f;
        float matchedWeight = 0.0f;

        List<LoveType> userLoveTypes = context.getLoveTypes();

        for (int i = 0; i < userLoveTypes.size(); i++) {
            LoveType loveType = userLoveTypes.get(i);
            // First 2 love types get weight 1.0, remaining get weight 0.5
            float weight = (i < 2) ? 1.0f : 0.5f;
            totalWeight += weight;

            if (pack.getLoveTypes().contains(loveType)) {
                matchedWeight += weight;
            }
        }

        float score = totalWeight > 0 ? matchedWeight / totalWeight : 0.0f;
        log.debug("Love type score for pack {}: matchedWeight={:.3f}, totalWeight={:.3f}, score={:.3f}",
                pack.getId(), matchedWeight, totalWeight, score);

        return score;
    }

    /**
     * Calculates relational need score using intersection method.
     * Score = intersection count / user needs count
     */
    private float calculateRelationalNeedScore(RitualPackDTO pack, RitualRecommendationContext context) {
        if (context.getRelationalNeeds() == null || context.getRelationalNeeds().isEmpty() ||
                pack.getRelationalNeeds() == null || pack.getRelationalNeeds().isEmpty()) {
            return 0.0f;
        }

        List<RelationalNeed> userNeeds = context.getRelationalNeeds();
        List<RelationalNeed> packNeeds = pack.getRelationalNeeds();

        long intersection = userNeeds.stream()
                .filter(packNeeds::contains)
                .count();

        float score = (float) intersection / userNeeds.size();

        log.debug("Relational need score for pack {}: intersection={}, userNeedsSize={}, score={:.3f}",
                pack.getId(), intersection, userNeeds.size(), score);

        return score;
    }

    /**
     * Calculates history penalty based on recommendation count.
     * historyPenalty = 1 / (1 + recommendationCount)
     */
    private float calculateHistoryPenalty(UUID packId, Map<UUID, Long> recommendationCounts) {
        long count = recommendationCounts.getOrDefault(packId, 0L);
        float penalty = 1.0f / (1.0f + count);

        if (count > 0) {
            log.debug("History penalty for pack {}: recommendationCount={}, penalty={:.3f}",
                    packId, count, penalty);
        }

        return penalty;
    }

    /**
     * Calculates recency penalty for the last recommended pack.
     * If pack was the last recommended, apply 0.3 multiplier.
     */
    private float calculateRecencyPenalty(UUID packId, UUID lastRecommendedPackId) {
        if (lastRecommendedPackId != null && packId.equals(lastRecommendedPackId)) {
            log.debug("Recency penalty applied to pack {}: penalty=0.3", packId);
            return 0.3f;
        }
        return 1.0f;
    }

    /**
     * Helper class to associate ritual packs with their match scores
     */
    @lombok.Value
    private static class ScoredPack {
        RitualPackDTO pack;
        float score;
    }
}
