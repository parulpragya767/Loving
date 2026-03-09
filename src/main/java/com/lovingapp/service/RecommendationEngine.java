package com.lovingapp.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.model.domain.RitualRecommendationContext;
import com.lovingapp.model.dto.RitualPackDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationEngine {

    private final RitualPackService ritualPackService;

    /**
     * Recommends ritual packs based on the recommendation context.
     * Uses a simple scoring system to rank ritual packs based on how well they
     * match the user's preferences and filters out previously recommended packs.
     * 
     * @param context The recommendation context containing preferences, needs, and
     *                history
     * @param limit   The maximum number of ritual packs to return
     * @return A list of recommended RitualPackDTOs, ordered by relevance score
     */
    @Transactional(readOnly = true)
    public List<RitualPackDTO> recommend(RitualRecommendationContext context, int limit) {
        // Get all available ritual packs
        List<RitualPackDTO> allPacks = ritualPackService.findAll();

        if (allPacks.isEmpty()) {
            log.info("No ritual packs available for recommendation");
            return List.of();
        }

        if (context == null) {
            log.info("Cannot score ritual packs: context is null (returning first {} packs)", limit);
            return allPacks.stream().limit(limit).collect(Collectors.toList());
        }

        // Filter out previously recommended packs
        List<RitualPackDTO> availablePacks = allPacks.stream()
                .filter(pack -> context.getRecommendationHistory() == null ||
                        context.getRecommendationHistory().stream()
                                .noneMatch(event -> event != null && pack.getId().equals(event.getRitualPackId())))
                .collect(Collectors.toList());

        if (availablePacks.isEmpty()) {
            log.info("All packs have been previously recommended, returning top scored packs anyway");
            availablePacks = allPacks;
        }

        // Score and sort ritual packs based on context
        List<RitualPackDTO> result = availablePacks.stream()
                .map(pack -> {
                    int score = calculateMatchScore(pack, context);
                    return new ScoredPack(pack, score);
                })
                .sorted(Comparator.comparingInt(ScoredPack::getScore).reversed())
                .limit(limit)
                .map(ScoredPack::getPack)
                .collect(Collectors.toList());

        log.info("Recommended {} ritual packs", result.size());
        return result;
    }

    /**
     * Calculates a match score for a ritual pack based on recommendation context.
     * Higher scores indicate better matches.
     */
    private int calculateMatchScore(RitualPackDTO pack, RitualRecommendationContext context) {
        int score = 0;

        // Match relational needs
        if (context.getRelationalNeeds() != null && pack.getRelationalNeeds() != null) {
            score += context.getRelationalNeeds().stream()
                    .filter(need -> pack.getRelationalNeeds().contains(need))
                    .count() * 3; // Higher weight for relational needs
        }

        // Match love languages
        if (context.getLoveTypes() != null && pack.getLoveTypes() != null) {
            score += context.getLoveTypes().stream()
                    .filter(loveType -> pack.getLoveTypes().contains(loveType))
                    .count() * 2;
        }

        return score;
    }

    /**
     * Helper class to associate ritual packs with their match scores
     */
    @lombok.Value
    private static class ScoredPack {
        RitualPackDTO pack;
        int score;
    }
}
