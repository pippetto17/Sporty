package model.utils;

/**
 * Application-wide constants to avoid string literal duplication.
 */
public final class Constants {

    private Constants() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Error messages - Login/Registration
    public static final String ERROR_USERNAME_EMPTY = "Username cannot be empty";
    public static final String ERROR_PASSWORD_EMPTY = "Password cannot be empty";
    public static final String ERROR_NAME_EMPTY = "Name cannot be empty";
    public static final String ERROR_SURNAME_EMPTY = "Surname cannot be empty";
    public static final String ERROR_USERNAME_EXISTS = "Username already exists";
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid username or password";
    public static final String ERROR_ALL_FIELDS_REQUIRED = "All fields are required";

    // Error messages - General
    public static final String ERROR_INVALID_OPTION = "Invalid option. Please try again.";
    public static final String ERROR_INVALID_MATCH_DETAILS = "Invalid match details. Please check your inputs.";
    public static final String ERROR_INVALID_FIELD_SELECTION = "Invalid field selection.";
    public static final String ERROR_INVALID_FIELD_NUMBER = "Invalid field number.";
    public static final String ERROR_SELECT_SPORT_FIRST = "Select a sport first";
    public static final String ERROR_UNEXPECTED = "Unexpected error: ";

    // Error messages - Validation
    public static final String ERROR_PLEASE_SELECT_SPORT = "Please select a sport";
    public static final String ERROR_PLEASE_SELECT_DATE = "Please select a date";
    public static final String ERROR_PLEASE_ENTER_TIME = "Please enter a time";
    public static final String ERROR_INVALID_TIME_FORMAT = "Invalid time format. Use HH:MM (e.g., 18:30)";
    public static final String ERROR_PLEASE_SELECT_CITY = "Please select a city from the list";
    public static final String ERROR_PLEASE_VALID_ITALIAN_CITY = "Please select a valid Italian city from the list";

    // Error messages - Initialization
    public static final String ERROR_DAO_INIT = "Failed to initialize DAO: ";
    public static final String ERROR_MATCH_SERVICE_INIT = "Errore nell'inizializzazione di MatchService: ";
    public static final String ERROR_FIELD_SERVICE_INIT = "Errore nell'inizializzazione di FieldService: ";
    public static final String ERROR_MATCHBEAN_NULL = "MatchBean non può essere null";
    public static final String ERROR_LOAD_ORGANIZE_MATCH_VIEW = "Failed to load organize match view: ";
    public static final String ERROR_UNEXPECTED_LOADING_VIEW = "Unexpected error loading organize match view: ";

    // Error messages - Match/Field
    public static final String ERROR_MATCH_NOT_FOUND = "Match not found";
    public static final String ERROR_USER_NOT_LOGGED_IN = "User not logged in";
    public static final String ERROR_NO_FIELD_SELECTED = "Nessun campo selezionato";
    public static final String ERROR_MATCH_CONFIRM = "Errore durante la conferma del match";

    // Error messages - DAO
    public static final String ERROR_LOADING_USERS_FS = "Error loading users from file system";
    public static final String ERROR_SAVING_USERS_FS = "Error saving users to file system";
    public static final String ERROR_SAVING_MATCH = "Error saving match: ";
    public static final String ERROR_FINDING_MATCH_BY_ID = "Error finding match by ID: ";
    public static final String ERROR_FINDING_MATCHES_BY_ORGANIZER = "Error finding matches by organizer: ";
    public static final String ERROR_FINDING_AVAILABLE_MATCHES = "Error finding available matches: ";
    public static final String ERROR_DELETING_MATCH = "Error deleting match: ";
    public static final String ERROR_FINDING_USER = "Error finding user by username";
    public static final String ERROR_SAVING_USER = "Error saving user";
    public static final String ERROR_FINDING_ALL_FIELDS = "Error finding all fields: ";
    public static final String ERROR_FINDING_FIELD = "Error finding field: ";
    public static final String ERROR_FINDING_FIELDS_BY_CITY = "Error finding fields by city: ";
    public static final String ERROR_FINDING_FIELDS_BY_SPORT = "Error finding fields by sport: ";
    public static final String ERROR_FINDING_AVAILABLE_FIELDS = "Error finding available fields: ";
    public static final String ERROR_SAVING_FIELD = "Error saving field: ";
    public static final String ERROR_DELETING_FIELD = "Error deleting field: ";

    // Success messages
    public static final String SUCCESS_JOIN_MATCH = "Successfully joined the match!";
    public static final String SUCCESS_MATCH_CANCELLED = "Match cancelled successfully";
    public static final String SUCCESS_MATCH_DETAILS_SAVED = "Match details saved successfully!";
    public static final String SUCCESS_REGISTRATION = "Registration successful!";
    public static final String SUCCESS_MATCH_DETAILS_FIELD_SELECTION = "Match details saved! Proceeding to field selection...";

    // Info messages
    public static final String INFO_INVITE_COMING_SOON = "Invite players feature coming soon!";
    public static final String INFO_PARTICIPANTS_TO_NOTIFY = "Participants to notify: ";

    // UI messages
    public static final String PROMPT_CHOOSE_OPTION = "Choose an option: ";
    public static final String PROMPT_YOUR_CHOICE = "Your choice: ";

    // UI messages - Match
    public static final String MSG_COULD_NOT_JOIN = "Could not join match. It may be full or you've already joined.";

    // Dialog titles
    public static final String DIALOG_TITLE_ERROR = "Error";

    // CSS class names
    public static final String CSS_SECONDARY_BUTTON = "secondary-button";
    public static final String CSS_MATCH_INFO = "match-info";
    public static final String CSS_FIELD_DETAIL = "field-detail";
    public static final String CSS_ERROR = "error";
    public static final String CSS_SUCCESS = "success";

    // Role names
    public static final String ROLE_PLAYER = "Player";
    public static final String ROLE_ORGANIZER = "Organizer";
    public static final String ROLE_FIELD_MANAGER = "Field Manager";

    // Separators
    public static final String SEPARATOR = "=================================";

    // Prefixes
    public static final String FOUND_PREFIX = "Found ";
}
