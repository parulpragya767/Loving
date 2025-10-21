package com.lovingapp.loving.model.dto;

import java.util.List;

import com.lovingapp.loving.model.enums.EmotionalState;
import com.lovingapp.loving.model.enums.LoveType;
import com.lovingapp.loving.model.enums.RelationalNeed;
import com.lovingapp.loving.model.enums.RitualMode;
import com.lovingapp.loving.model.enums.RitualType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RitualFilterRequest {
    private List<LoveType> loveTypes;
    private List<RitualType> ritualTypes;
    private List<RitualMode> ritualModes;
    private List<EmotionalState> emotionalStates;
    private List<RelationalNeed> relationalNeeds;
}
