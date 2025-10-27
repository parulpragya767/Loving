package com.lovingapp.loving.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(enumAsRef = true)
public enum RelationalNeed {
    CONNECTION("Connection"),
    APPRECIATION("Appreciation"),
    RESPECT("Respect"),
    TRUST("Trust"),
    INTIMACY("Intimacy"),
    SUPPORT("Support"),
    ACCEPTANCE("Acceptance"),
    SPACE("Space"),
    SECURITY("Security"),
    ADVENTURE("Adventure");

    private final String displayName;

    @Override
    public String toString() {
        return displayName;
    }
}
