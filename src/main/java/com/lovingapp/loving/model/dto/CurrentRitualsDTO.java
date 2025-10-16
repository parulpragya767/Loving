package com.lovingapp.loving.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentRitualsDTO {
    private List<RitualHistoryDTO> ritualHistory;
    private List<RitualPackDTO> ritualPacks;
    private List<RitualDTO> rituals;
}
