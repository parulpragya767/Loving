package com.lovingapp.loving.model.enums;

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

    RelationalNeed(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
