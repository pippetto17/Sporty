package model.domain;

public enum Sport {
    FOOTBALL_5(0, 10, "Calcio a 5"),
    FOOTBALL_8(1, 16, "Calcio a 8"),
    FOOTBALL_11(2, 22, "Calcio a 11"),
    BASKETBALL(3, 10, "Basket"),
    TENNIS_SINGLE(4, 2, "Tennis Singolo"),
    TENNIS_DOUBLE(5, 4, "Tennis Doppio"),
    PADEL_SINGLE(6, 2, "Padel Singolo"),
    PADEL_DOUBLE(7, 4, "Padel Doppio");

    private final int code;
    private final int requiredPlayers;
    private final int duration;
    private final String displayName;

    Sport(int code, int requiredPlayers, String displayName) {
        this(code, requiredPlayers, 120, displayName);
    }

    Sport(int code, int requiredPlayers, int duration, String displayName) {
        this.code = code;
        this.requiredPlayers = requiredPlayers;
        this.duration = duration;
        this.displayName = displayName;
    }

    public int getCode() {
        return code;
    }

    public int getRequiredPlayers() {
        return requiredPlayers;
    }

    public int getDuration() {
        return duration;
    }

    public int getAdditionalParticipantsNeeded() {
        return requiredPlayers - 1;
    }

    public boolean isValidAdditionalParticipants(int additionalParticipants) {
        return additionalParticipants >= 1 && additionalParticipants <= getAdditionalParticipantsNeeded();
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Sport fromCode(int code) {
        for (Sport sport : Sport.values()) {
            if (sport.getCode() == code) {
                return sport;
            }
        }
        throw new IllegalArgumentException("Invalid sport code: " + code);
    }

    @Override
    public String toString() {
        return displayName + " (" + requiredPlayers + " players)";
    }
}
