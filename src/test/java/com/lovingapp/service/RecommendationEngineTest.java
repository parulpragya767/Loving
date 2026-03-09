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

        // Verify the expected ritual pack is in the recommendations (should be first
        // due to high score)
        RitualPackDTO firstRecommendation = recommendations.get(0);
        assertEquals(testCase.getExpectedRitualPackId(), firstRecommendation.getId().toString());
        assertEquals(testCase.getExpectedRitualPackTitle(), firstRecommendation.getTitle());

        // Verify that the recommendation has matching attributes based on context
        for (LoveType loveType : context.getLoveTypes()) {
            assertTrue(firstRecommendation.getLoveTypes().contains(loveType),
                    "Expected love type " + loveType + " not found in recommendation");
        }

        for (RelationalNeed relationalNeed : context.getRelationalNeeds()) {
            assertTrue(firstRecommendation.getRelationalNeeds().contains(relationalNeed),
                    "Expected relational need " + relationalNeed + " not found in recommendation");
        }

        // verify(ritualPackService).findAll();
    }

    @Test
    void testRecommendWithNullContext() {
        // When
        List<RitualPackDTO> recommendations = recommendationEngine.recommend(null, 3);

        // Then
        assertNotNull(recommendations);
        assertEquals(3, recommendations.size());
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
