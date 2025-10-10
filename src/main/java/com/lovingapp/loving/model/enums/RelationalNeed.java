package com.lovingapp.loving.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
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
