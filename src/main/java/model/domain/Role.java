package model.domain;

public enum Role {
    PLAYER(1),
    ORGANIZER(2);

    private final int code;

    Role(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public String getDisplayName() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }

    public static Role fromCode(int code) {
        for (Role role : Role.values()) {
            if (role.code == code) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role code: " + code);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
