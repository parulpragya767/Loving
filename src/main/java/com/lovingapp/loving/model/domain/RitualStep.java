package com.lovingapp.loving.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RitualStep {
    private String title;
    private String description;
    private Integer order;
    private Integer durationMinutes;
    private boolean optional;
    private String materials;
    private String tips;
}
