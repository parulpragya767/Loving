package com.lovingapp.loving.model.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.lovingapp.loving.model.enums.EmojiFeedback;
import com.lovingapp.loving.model.enums.RitualHistoryStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class RitualHistoryDTO {
    private UUID id;
    private UUID userId;
    private UUID ritualId;
    private UUID ritualPackId;
    private RitualHistoryStatus status;
    private EmojiFeedback feedback;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
