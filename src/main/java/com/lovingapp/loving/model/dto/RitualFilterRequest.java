package com.lovingapp.loving.model.dto;

import java.util.List;

import com.lovingapp.loving.model.enums.LoveType;
import com.lovingapp.loving.model.enums.RelationalNeed;
import com.lovingapp.loving.model.enums.RitualMode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RitualFilterRequest {
    private List<LoveType> loveTypes;
    private List<RitualMode> ritualModes;
    private List<RelationalNeed> relationalNeeds;
}
