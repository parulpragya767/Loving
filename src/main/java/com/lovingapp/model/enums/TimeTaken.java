package com.lovingapp.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true)
public enum TimeTaken {
    MOMENT("< 1 minute", "< 1 minute"),
    SHORT("1-5 minutes", "1-5 minutes"),
    MEDIUM("5-15 minutes", "5-15 minutes"),
    LONG("15-30 minutes", "15-30 minutes"),
    EXTENDED("30+ minutes", "30+ minutes"),
    FLEXIBLE("Flexible", "Flexible / As long as you wish");

    private final String displayName;
    private final String description;
}
