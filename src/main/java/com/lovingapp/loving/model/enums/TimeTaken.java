package com.lovingapp.loving.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true)
public enum TimeTaken {
    MOMENT("< 1 minute"),
    SHORT("1-5 minutes"),
    MEDIUM("5-15 minutes"),
    LONG("15-30 minutes"),
    EXTENDED("30+ minutes"),
    FLEXIBLE("Flexible / As long as you wish");

    private final String displayName;
}
