package com.asdc.unicarpool.model;

public enum RideStatus {
    WAITING("Waiting"),
    STARTED("Started"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String displayName;

    RideStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
