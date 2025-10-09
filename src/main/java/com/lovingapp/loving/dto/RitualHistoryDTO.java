package com.lovingapp.loving.dto;

import com.lovingapp.loving.model.enums.EmojiFeedback;
import com.lovingapp.loving.model.enums.RitualHistoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

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
    private OffsetDateTime occurredAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
