package model.domain;

/**
 * Enum representing the type of booking.
 * MATCH: Booking made for an organized match
 * PRIVATE: Private booking by individual/group
 */
public enum BookingType {
    MATCH("Match Booking"),
    PRIVATE("Private Booking");

    private final String displayName;

    BookingType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
