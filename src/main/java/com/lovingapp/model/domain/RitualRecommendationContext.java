package com.lovingapp.model.domain;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.lovingapp.model.enums.Journey;
import com.lovingapp.model.enums.LoveType;
import com.lovingapp.model.enums.RelationalNeed;
import com.lovingapp.model.enums.RelationshipStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RitualRecommendationContext {

    private Journey journey;

    private List<LoveType> loveTypes;

    private List<RelationalNeed> relationalNeeds;

    private RelationshipStatus relationshipStatus;

    private String semanticSummary;

    private List<RitualPackRecommendationEvent> recommendationHistory;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RitualPackRecommendationEvent {
        private UUID ritualPackId;
        private OffsetDateTime recommendedAt;
    }
}
