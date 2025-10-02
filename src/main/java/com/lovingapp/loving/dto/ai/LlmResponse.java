package com.lovingapp.loving.dto.ai;

import com.lovingapp.loving.model.enums.EmotionalState;
import com.lovingapp.loving.model.enums.LoveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmResponse {
    private String response;
    private Context context;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Context {
        private List<EmotionalState> emotionalStates;
        private List<LoveType> loveTypes;
        private boolean needsFollowUp;
        private boolean readyForRecommendation;
    }
}
