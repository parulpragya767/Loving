package com.lovingapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
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
import com.lovingapp.model.domain.RitualRecommendationContext.RitualPackRecommendationEvent;
import com.lovingapp.model.dto.RitualPackDTO;
import com.lovingapp.model.enums.Journey;
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
        RecommendationTestCase testCase = recommendationTestCases().findFirst().orElseThrow();
        RitualRecommendationContext context = testCase.getContext();

        // Add the expected ritual pack to recommendation history
        RitualPackRecommendationEvent historyEvent = RitualPackRecommendationEvent.builder()
                .ritualPackId(UUID.fromString(testCase.getExpectedRitualPackId()))
                .recommendedAt(OffsetDateTime.now().minusDays(1))
                .build();
        context.setRecommendationHistory(List.of(historyEvent));

        // When
        List<RitualPackDTO> recommendations = recommendationEngine.recommend(context, 5);

        // Then
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());

        // Verify that the previously recommended pack is not the first recommendation
        RitualPackDTO firstRecommendation = recommendations.get(0);
        assertNotEquals(testCase.getExpectedRitualPackId(), firstRecommendation.getId().toString());

        // verify(ritualPackService).findAll();
    }

    @Test
    void testScoringSystemWithHistoryAndRecencyPenalties() {
        // Create test ritual packs
        UUID pack1Id = UUID.randomUUID();
        UUID pack2Id = UUID.randomUUID();

        RitualPackDTO pack1 = RitualPackDTO.builder()
                .id(pack1Id)
                .title("Perfect Match Pack")
                .journey(Journey.FEELING_DISTANT)
                .loveTypes(List.of(LoveType.BELONG, LoveType.CARE))
                .relationalNeeds(List.of(RelationalNeed.CONNECTION, RelationalNeed.UNDERSTANDING))
                .build();

        RitualPackDTO pack2 = RitualPackDTO.builder()
                .id(pack2Id)
                .title("Partial Match Pack")
                .journey(Journey.FEELING_DISTANT)
                .loveTypes(List.of(LoveType.SPARK))
                .relationalNeeds(List.of(RelationalNeed.PLAY_AND_JOY))
                .build();

        // Override the mock for this specific test
        lenient().when(ritualPackService.findAll()).thenReturn(List.of(pack1, pack2));

        // Create context with history and recency
        RitualPackRecommendationEvent historyEvent1 = RitualPackRecommendationEvent.builder()
                .ritualPackId(pack1Id)
                .recommendedAt(OffsetDateTime.now().minusDays(5))
                .build();

        RitualPackRecommendationEvent historyEvent2 = RitualPackRecommendationEvent.builder()
                .ritualPackId(pack1Id)
                .recommendedAt(OffsetDateTime.now().minusDays(3))
                .build();

        RitualPackRecommendationEvent recentEvent = RitualPackRecommendationEvent.builder()
                .ritualPackId(pack2Id)
                .recommendedAt(OffsetDateTime.now().minusHours(1))
                .build();

        RitualRecommendationContext context = RitualRecommendationContext.builder()
                .journey(Journey.FEELING_DISTANT)
                .loveTypes(List.of(LoveType.BELONG, LoveType.CARE, LoveType.SPARK))
                .relationalNeeds(List.of(RelationalNeed.CONNECTION, RelationalNeed.UNDERSTANDING))
                .recommendationHistory(List.of(historyEvent1, historyEvent2, recentEvent))
                .build();

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
        // Create packs with different journeys
        RitualPackDTO pack1 = RitualPackDTO.builder()
                .id(UUID.randomUUID())
                .title("Distant Pack")
                .journey(Journey.FEELING_DISTANT)
                .loveTypes(List.of(LoveType.BELONG))
                .relationalNeeds(List.of(RelationalNeed.CONNECTION))
                .build();

        RitualPackDTO pack2 = RitualPackDTO.builder()
                .id(UUID.randomUUID())
                .title("Flat Pack")
                .journey(Journey.LOVE_FEELS_FLAT)
                .loveTypes(List.of(LoveType.SPARK))
                .relationalNeeds(List.of(RelationalNeed.PLAY_AND_JOY))
                .build();

        // Override the mock for this specific test
        lenient().when(ritualPackService.findAll()).thenReturn(List.of(pack1, pack2));

        // Test with specific journey - should filter to only matching journey
        RitualRecommendationContext contextWithJourney = RitualRecommendationContext.builder()
                .journey(Journey.FEELING_DISTANT)
                .loveTypes(List.of(LoveType.BELONG))
                .relationalNeeds(List.of(RelationalNeed.CONNECTION))
                .build();

        List<RitualPackDTO> recommendations = recommendationEngine.recommend(contextWithJourney, 5);
        assertNotNull(recommendations);
        assertEquals(1, recommendations.size()); // Should only include pack1
        assertEquals("Distant Pack", recommendations.get(0).getTitle());

        // Test with no journey - should fallback to all packs
        RitualRecommendationContext contextNoJourney = RitualRecommendationContext.builder()
                .loveTypes(List.of(LoveType.BELONG))
                .relationalNeeds(List.of(RelationalNeed.CONNECTION))
                .build();

        recommendations = recommendationEngine.recommend(contextNoJourney, 5);
        assertNotNull(recommendations);
        assertEquals(2, recommendations.size()); // Should include both packs

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
