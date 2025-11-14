package com.lovingapp.loving.model.dto;

import java.util.List;

import com.lovingapp.loving.model.enums.LoveType;
import com.lovingapp.loving.model.enums.PublicationStatus;
import com.lovingapp.loving.model.enums.RelationalNeed;
import com.lovingapp.loving.model.enums.RitualMode;
import com.lovingapp.loving.model.enums.RitualTone;
import com.lovingapp.loving.model.enums.TimeTaken;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RitualFilterDTO {
    private List<LoveType> loveTypes;
    private List<RitualMode> ritualModes;
    private List<TimeTaken> timeTaken;
    private List<RelationalNeed> relationalNeeds;
    private List<RitualTone> ritualTones;
    private PublicationStatus status;
}
