package com.lovingapp.loving.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RitualTagsDTO {
    private RitualTagDTO loveTypes;
    private RitualTagDTO ritualModes;
    private RitualTagDTO relationalNeeds;
}