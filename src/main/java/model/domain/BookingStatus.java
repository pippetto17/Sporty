package model.domain;

/**
 * Enum representing booking status with state machine transitions.
 * PENDING -> CONFIRMED, REJECTED, or CANCELLED
 * CONFIRMED -> CANCELLED or COMPLETED
 * REJECTED, CANCELLED, COMPLETED -> terminal states
 */
public enum BookingStatus {
    PENDING(0, "Pending Approval"),
    CONFIRMED(1, "Confirmed"),
    REJECTED(2, "Rejected"),
    CANCELLED(3, "Cancelled"),
    COMPLETED(4, "Completed");

    private final int code;
    private final String displayName;

    BookingStatus(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public int getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get BookingStatus from code.
     */
    public static BookingStatus fromCode(int code) {
        for (BookingStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid booking status code: " + code);
    }

    /**
     * Validate state transition (State Machine pattern).
     */
    public boolean canTransitionTo(BookingStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == CONFIRMED || newStatus == REJECTED || newStatus == CANCELLED;
            case CONFIRMED -> newStatus == CANCELLED || newStatus == COMPLETED;
            case REJECTED, CANCELLED, COMPLETED -> false;
        };
    }
}
