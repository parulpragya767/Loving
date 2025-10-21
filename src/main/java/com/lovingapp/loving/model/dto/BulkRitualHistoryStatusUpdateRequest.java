package com.lovingapp.loving.model.dto;

import java.util.List;
import java.util.UUID;

import com.lovingapp.loving.model.enums.RitualHistoryStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BulkRitualHistoryStatusUpdateRequest {
    @NotNull(message = "Status updates cannot be null")
    @Valid
    private List<RitualStatusUpdate> updates;

    @Data
    public static class RitualStatusUpdate {
        @NotNull(message = "Ritual history ID cannot be null")
        private UUID ritualHistoryId;

        @NotNull(message = "Status cannot be null")
        private RitualHistoryStatus status;
    }
}
