package model.utils;

/**
 * Application-wide constants to avoid string literal duplication.
 */
public final class Constants {

    private Constants() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Error messages
    public static final String ERROR_USERNAME_EMPTY = "Username cannot be empty";
    public static final String ERROR_PASSWORD_EMPTY = "Password cannot be empty";
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid username or password";
    public static final String ERROR_INVALID_OPTION = "Invalid option. Please try again.";
    public static final String ERROR_INVALID_MATCH_DETAILS = "Invalid match details. Please check your inputs.";
    public static final String ERROR_INVALID_FIELD_SELECTION = "Invalid field selection.";
    public static final String ERROR_INVALID_FIELD_NUMBER = "Invalid field number.";
    public static final String ERROR_SELECT_SPORT_FIRST = "Select a sport first";
    public static final String ERROR_UNEXPECTED = "Unexpected error: ";
    public static final String ERROR_MATCH_SERVICE_INIT = "Errore nell'inizializzazione di MatchService: ";
    public static final String ERROR_MATCHBEAN_NULL = "MatchBean non può essere null";

    // UI messages
    public static final String PROMPT_CHOOSE_OPTION = "Choose an option: ";

    // CSS class names
    public static final String CSS_SECONDARY_BUTTON = "secondary-button";
    public static final String CSS_MATCH_INFO = "match-info";

    // Separators
    public static final String SEPARATOR = "=================================";

    // Prefixes
    public static final String FOUND_PREFIX = "Found ";
}

