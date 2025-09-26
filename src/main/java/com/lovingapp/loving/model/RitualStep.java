package com.lovingapp.loving.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class RitualStep {
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "step_order")
    private Integer order;
    
    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    
    @Column(name = "is_optional")
    private boolean optional = false;
    
    @Column(name = "materials")
    private String materials; // Comma-separated list of required materials
    
    @Column(name = "tips", columnDefinition = "TEXT")
    private String tips;
}
