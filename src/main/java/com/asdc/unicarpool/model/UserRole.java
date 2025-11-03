package com.asdc.unicarpool.model;

public enum UserRole {
    RIDER("Rider"),
    DRIVER("Driver");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
