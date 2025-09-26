package com.lovingapp.loving.model;

import lombok.*;

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
