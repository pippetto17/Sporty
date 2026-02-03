package model.domain;
public enum MatchStatus {
    PENDING(0, "Pending"),
    APPROVED(1, "Approved"),
    REJECTED(2, "Rejected");
    private final int code;
    private final String displayName;
    MatchStatus(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    public int getCode() {
        return code;
    }
    public String getDisplayName() {
        return displayName;
    }
    public static MatchStatus fromCode(int code) {
        for (MatchStatus status : MatchStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid match status code: " + code);
    }
    @Override
    public String toString() {
        return displayName;
    }
}