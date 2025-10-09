package com.lovingapp.loving.dto;

import com.lovingapp.loving.model.enums.EmojiFeedback;
import com.lovingapp.loving.model.enums.RitualHistoryStatus;

import lombok.Data;

@Data
public class RitualHistoryStatusUpdateRequest {
    private RitualHistoryStatus status; // optional for complete endpoint; required for generic status change
    private EmojiFeedback feedback; // optional
}
