package com.lovingapp.loving.model.enums;

public enum LoveType {
    ROMANTIC_LOVE("Romantic Love"),
    PLATONIC_LOVE("Platonic Love"),
    FAMILIAL_LOVE("Familial Love"),
    SELF_LOVE("Self-Love"),
    COMPANIONATE_LOVE("Companionate Love");

    private final String displayName;

    LoveType(String displayName) {
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
