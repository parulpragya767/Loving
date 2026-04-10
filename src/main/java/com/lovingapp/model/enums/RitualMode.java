package com.lovingapp.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true)
public enum RitualMode {
    SOLO("Solo", "Individual practice."),
    TOGETHER("Together", "Shared partner practice.");

    private final String displayName;
    private final String description;
}
