package com.lovingapp.loving.model.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.lovingapp.loving.model.dto.RitualHistoryDTOs.RitualHistoryDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentRitualsDTO {
    private Map<UUID, List<RitualHistoryDTO>> ritualHistoryMap;
    private List<RitualPackDTO> ritualPacks;
    private List<RitualDTO> rituals;
}
