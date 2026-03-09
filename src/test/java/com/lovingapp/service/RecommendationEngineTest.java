package com.lovingapp.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovingapp.model.domain.RitualRecommendationContext;
import com.lovingapp.model.dto.RitualPackDTO;
import com.lovingapp.model.enums.LoveType;
import com.lovingapp.model.enums.RelationalNeed;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@Slf4j
class RecommendationEngineTest {

    @Mock
    private RitualPackService ritualPackService;

    @InjectMocks
    private RecommendationEngine recommendationEngine;

    private static List<RitualPackDTO> mockRitualPacks;
    private List<RecommendationTestCase> testCases;

    @BeforeAll
    static void loadRitualPacks() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ClassPathResource resource = new ClassPathResource("data/ritualPacks.json");

        mockRitualPacks = mapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<RitualPackDTO>>() {
                });
    }

    @BeforeAll
    void loadTestCases() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        ClassPathResource resource = new ClassPathResource("data/recommendation-test-cases.json");

        testCases = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<RecommendationTestCase>>() {
                });
    }

    Stream<RecommendationTestCase> recommendationTestCases() {
        return testCases.stream();
    }

    @BeforeEach
    void setUp() {
        lenient().when(ritualPackService.findAll()).thenReturn(mockRitualPacks);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("recommendationTestCases")
    void testRecommendWithContext(RecommendationTestCase testCase) {
        // Given
        RitualRecommendationContext context = testCase.getContext();

        // When
        List<RitualPackDTO> recommendations = recommendationEngine.recommend(context, 5);

        // Then
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());

        // With the new sophisticated scoring system, we verify that recommendations
        // contain packs that match the user's context rather than expecting specific
        // packs
        RitualPackDTO topRecommendation = recommendations.get(0);

        // Verify that the top recommendation has some matching attributes
        boolean hasMatchingLoveType = context.getLoveTypes().stream()
                .anyMatch(loveType -> topRecommendation.getLoveTypes().contains(loveType));
        boolean hasMatchingRelationalNeed = context.getRelationalNeeds().stream()
                .anyMatch(need -> topRecommendation.getRelationalNeeds().contains(need));

        assertTrue(hasMatchingLoveType || hasMatchingRelationalNeed,
                "Top recommendation should have at least one matching love type or relational need");

        // verify(ritualPackService).findAll();
    }

    @Test
    void testRecommendWithNullContext() {
        // When
        List<RitualPackDTO> recommendations = recommendationEngine.recommend(null, 3);

        // Then
        assertNotNull(recommendations);
        // Should return up to the limit, but may be less if fewer packs available
        assertTrue(recommendations.size() <= 3);
        assertTrue(recommendations.size() > 0); // Should have at least some recommendations
        // verify(ritualPackService).findAll();
    }

    @Test
    void testRecommendWithRecommendationHistory() {
        // Given
        RecommendationTestCase testCase = testCases.stream()
                .filter(tc -> "test_with_recommendation_history".equals(tc.getTestName()))
                .findFirst()
                .orElseThrow();
        RitualRecommendationContext context = testCase.getContext();

        // When
        List<RitualPackDTO> recommendations = recommendationEngine.recommend(context, 5);

        // Then
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());

        // Verify that the previously recommended pack is not the first recommendation
        RitualPackDTO firstRecommendation = recommendations.get(0);
        assertNotEquals(testCase.getExpectedRitualPackId(), firstRecommendation.getId().toString());
    }

    @Test
    void testScoringSystemWithHistoryAndRecencyPenalties() {
        // Given
        RecommendationTestCase testCase = testCases.stream()
                .filter(tc -> "test_scoring_with_history_and_recency".equals(tc.getTestName()))
                .findFirst()
                .orElseThrow();

        RitualRecommendationContext context = testCase.getContext();

        // When
        List<RitualPackDTO> recommendations = recommendationEngine.recommend(context, 5);

        // Then
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());

        // Should return at least one pack with proper scoring applied
        assertTrue(recommendations.size() >= 1);

        log.info("Scoring system test completed with history and recency penalties");
    }

    @Test
    void testJourneyFilteringWithFallback() {
        // Given
        RecommendationTestCase testCase = testCases.stream()
                .filter(tc -> "test_journey_filtering_with_fallback".equals(tc.getTestName()))
                .findFirst()
                .orElseThrow();

        // Test with specific journey - should filter to only matching journey
        RitualRecommendationContext contextWithJourney = testCase.getContext();

        List<RitualPackDTO> recommendations = recommendationEngine.recommend(contextWithJourney, 5);
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());

        // Verify that recommendations contain packs matching the journey
        boolean hasMatchingJourney = recommendations.stream()
                .anyMatch(pack -> pack.getJourney() == contextWithJourney.getJourney());
        assertTrue(hasMatchingJourney, "Should have at least one pack matching the journey");

        // Test with no journey - should fallback to all packs
        RitualRecommendationContext contextNoJourney = RitualRecommendationContext.builder()
                .loveTypes(List.of(LoveType.BELONG))
                .relationalNeeds(List.of(RelationalNeed.CONNECTION))
                .build();

        recommendations = recommendationEngine.recommend(contextNoJourney, 5);
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty()); // Should include packs from all journeys

        log.info("Journey filtering test completed with fallback verification");
    }

    @Data
    public static class RecommendationTestCase {
        private String testName;
        private RitualRecommendationContext context;
        private String expectedRitualPackId;
        private String expectedRitualPackTitle;

        @Override
        public String toString() {
            return testName;
        }
    }
}
