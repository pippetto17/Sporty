package model.domain;

public enum Sport {
    FOOTBALL_5(0, 10, "Calcio a 5"), // 5 vs 5 = 10 totali
    FOOTBALL_8(1, 16, "Calcio a 8"), // 8 vs 8 = 16 totali
    FOOTBALL_11(2, 22, "Calcio a 11"), // 11 vs 11 = 22 totali
    BASKETBALL(3, 10, "Basket"), // 5 vs 5 = 10 totali
    TENNIS_SINGLE(4, 2, "Tennis Singolo"), // 1 vs 1 = 2 totali
    TENNIS_DOUBLE(5, 4, "Tennis Doppio"), // 2 vs 2 = 4 totali
    PADEL_SINGLE(6, 2, "Padel Singolo"), // 1 vs 1 = 2 totali
    PADEL_DOUBLE(7, 4, "Padel Doppio"); // 2 vs 2 = 4 totali

    private final int code;
    private final int requiredPlayers; // Numero TOTALE di giocatori per match completo
    private final int duration; // Durata in minuti
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

    /**
     * Restituisce il numero di partecipanti AGGIUNTIVI necessari
     * (escludendo l'organizer che è già incluso)
     * L'organizer conta come primo giocatore, quindi servono (requiredPlayers - 1)
     * altri
     */
    public int getAdditionalParticipantsNeeded() {
        return requiredPlayers - 1;
    }

    /**
     * Valida se il numero di partecipanti aggiuntivi richiesti è valido
     * (deve essere tra 1 e requiredPlayers-1)
     */
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
