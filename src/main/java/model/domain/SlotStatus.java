package model.domain;

/**
 * Enum representing time slot status.
 */
public enum SlotStatus {
    AVAILABLE("Available"),
    BOOKED("Booked"),
    BLOCKED("Blocked"); // For maintenance or closures

    private final String displayName;

    SlotStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
