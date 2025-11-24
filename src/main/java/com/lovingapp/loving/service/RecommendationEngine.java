package com.lovingapp.loving.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.loving.model.dto.RitualPackDTO;
import com.lovingapp.loving.model.dto.UserContextDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationEngine {

    private final RitualPackService ritualPackService;

    /**
     * Recommends a ritual pack based on the user's context.
     * Uses a simple scoring system to rank ritual packs based on how well they
     * match the user's preferences.
     * 
     * @param userContext The user's context containing preferences and needs
     * @return An Optional containing the recommended RitualPackDTO if available,
     *         empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<RitualPackDTO> recommendRitualPack(UserContextDTO userContext) {
        // Get all available ritual packs
        List<RitualPackDTO> allPacks = ritualPackService.findAll();

        if (allPacks.isEmpty()) {
            log.warn("No ritual packs available for recommendation");
            // If no packs are available, return empty
            return Optional.empty();
        }

        if (userContext == null) {
            log.warn("Cannot recommend ritual pack: user context is null");
            // return Optional.empty();
            // If you want to return the first pack when no context is available, use:
            return Optional.of(allPacks.get(0));
        }

        // Score and sort ritual packs based on user context
        return allPacks.stream()
                .map(pack -> {
                    int score = calculateMatchScore(pack, userContext);
                    log.debug("Pack '{}' score: {}", pack.getTitle(), score);
                    return new ScoredPack(pack, score);
                })
                .sorted(Comparator.comparingInt(ScoredPack::getScore).reversed())
                .findFirst()
                .map(ScoredPack::getPack);
    }

    /**
     * Calculates a match score for a ritual pack based on user context.
     * Higher scores indicate better matches.
     */
    private int calculateMatchScore(RitualPackDTO pack, UserContextDTO userContext) {
        int score = 0;

        // Match relational needs
        if (userContext.getRelationalNeeds() != null && pack.getRelationalNeeds() != null) {
            score += userContext.getRelationalNeeds().stream()
                    .filter(need -> pack.getRelationalNeeds().contains(need))
                    .count() * 3; // Higher weight for relational needs
        }

        // Match love languages
        if (userContext.getLoveTypes() != null && pack.getLoveTypes() != null) {
            score += userContext.getLoveTypes().stream()
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
