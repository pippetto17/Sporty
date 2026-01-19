package model.utils;

/**
 * Application-wide constants to avoid string literal duplication.
 */
public final class Constants {

    private Constants() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // CLI/UI helpers
    // Separator used by CLI views to draw simple lines
    public static final String SEPARATOR = "----------------------------------------";
    // Prefix used when reporting how many items were found
    public static final String FOUND_PREFIX = "Found ";

    // Roles used across the app (display strings)
    public static final String ROLE_PLAYER = "Player";
    public static final String ROLE_ORGANIZER = "Organizer";
    public static final String ROLE_FIELD_MANAGER = "Field Manager";

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
    public static final String DIALOG_TITLE_MATCH_DETAILS = "Match Details";
    public static final String DIALOG_TITLE_LOGOUT = "Logout";
    public static final String DIALOG_TITLE_BOOK_FIELD = "Book Field";

    // UI Labels and Messages - Home View
    public static final String LABEL_AVAILABLE_MATCHES = "Available Matches";
    public static final String LABEL_MY_ORGANIZED_MATCHES = "My Organized Matches";
    public static final String LABEL_NO_MATCHES = "No matches available";
    public static final String LABEL_FILTERS = "Filters:";
    public static final String LABEL_WELCOME_PREFIX = "Welcome, ";
    public static final String LABEL_WELCOME_SUFFIX = "!";

    // Button Labels
    public static final String BTN_PLAYER = "Player";
    public static final String BTN_ORGANIZER = "Organizer";
    public static final String BTN_APPLY = "Apply";
    public static final String BTN_CLEAR = "Clear";
    public static final String BTN_REFRESH = "Refresh";
    public static final String BTN_SEARCH = "Search";
    public static final String BTN_JOIN_MATCH = "Join Match";

    // Prompt Text
    public static final String PROMPT_SPORT = "Sport";
    public static final String PROMPT_CITY = "City";
    public static final String PROMPT_DATE = "Date";
    public static final String PROMPT_SEARCH_DESTINATION = "Search destination";
    public static final String PROMPT_ANY = "Any";
    public static final String PROMPT_ADD_DATES = "Add dates";

    // Status Messages - Home View
    public static final String STATUS_READY = "Ready";
    public static final String STATUS_MATCH_NOT_FOUND = "Match not found";
    public static final String STATUS_OPENING_ORGANIZE_MATCH = "Opening organize match...";
    public static final String STATUS_OPENING_BOOK_FIELD = "Opening book field...";
    public static final String STATUS_REFRESHING = "Refreshing...";
    public static final String STATUS_LOGGING_OUT = "Logging out...";
    public static final String STATUS_FILTERS_CLEARED = "Filters cleared";

    // Match Details Dialog Labels
    public static final String MATCH_DETAIL_DATE = "📅 Date: ";
    public static final String MATCH_DETAIL_AT = " at ";
    public static final String MATCH_DETAIL_CITY = "📍 City: ";
    public static final String MATCH_DETAIL_ORGANIZER = "👤 Organizer: ";
    public static final String MATCH_DETAIL_PLAYERS = "👥 Players: ";
    public static final String MATCH_DETAIL_PRICE = "💰 Price: €";
    public static final String MATCH_DETAIL_STATUS = "📊 Status: ";
    public static final String MATCH_DETAIL_FREE = "Free";
    public static final String MATCH_DETAIL_SEPARATOR = "/";

    // Match Card Labels
    public static final String MATCH_CARD_SPORT_PREFIX = "⚽ ";
    public static final String MATCH_CARD_DATE_PREFIX = "📅 ";
    public static final String MATCH_CARD_PLAYERS_PREFIX = "👥 Players: ";
    public static final String MATCH_CARD_LOCATION_PREFIX = "📍 ";

    // Messages
    public static final String MSG_BOOK_FIELD_COMING_SOON = "Book field feature coming soon!";
    public static final String MSG_LOGOUT_CONFIRM = "Are you sure you want to logout?";
    public static final String MSG_FILTERS_APPLIED_PREFIX = "Filters applied - ";
    public static final String MSG_FILTERS_APPLIED_SUFFIX = " matches found";
    public static final String MSG_MATCHES_LOADED_SUFFIX = " matches loaded";
    public static final String MSG_ERROR_MATCH_DETAILS = "Error showing match details: ";

    // Window Titles
    public static final String WINDOW_TITLE_SPORTY_HOME = "Sporty - Home";

    // Error Messages - Home View
    public static final String ERROR_LOAD_HOME_VIEW = "Failed to load home view: ";

    // FXML Paths
    public static final String FXML_PATH_HOME = "/fxml/home.fxml";

    // CSS Paths
    public static final String CSS_PATH_STYLE = "/css/style.css";
    public static final String CSS_PATH_CONTROLS_DARK = "/css/controls-dark.css";

    // CSS class names
    public static final String CSS_SECONDARY_BUTTON = "secondary-button";
    public static final String CSS_MATCH_INFO = "match-info";
    public static final String CSS_FIELD_DETAIL = "field-detail";
    public static final String CSS_ERROR = "danger";
    public static final String CSS_SUCCESS = "success";
    public static final String CSS_INFO = "info";
    public static final String CSS_FIELD_LABEL = "field-label";
    public static final String CSS_CUSTOM_TEXT_FIELD = "custom-text-field";
    public static final String CSS_CUSTOM_COMBO_BOX = "custom-combo-box";
    public static final String CSS_CUSTOM_DATE_PICKER = "custom-date-picker";
    public static final String CSS_PRIMARY_BUTTON = "primary-button";
    public static final String CSS_MATCH_CARD = "match-card";
    public static final String CSS_MATCH_TITLE = "match-title";
    public static final String CSS_STATUS_BADE = "status-badge";
    public static final String CSS_ROLE_SWITCH_CONTAINER = "role-switch-container";
    public static final String CSS_ROLE_SWITCH_BUTTON = "role-switch-button";
    public static final String CSS_ACTIVE = "active";
    public static final String CSS_ACCENT = "accent";
    // Common CSS helpers used by views
    public static final String CSS_SEARCH_FIELD_CONTAINER = "search-field-container";
    public static final String CSS_TEXT_CAPTION = "text-caption";
    public static final String CSS_TEXT_MUTED = "text-muted";
    public static final String CSS_TITLE_4 = "title-4";

    // Home view specific labels and UI strings
    public static final String LABEL_WHERE = "Where";
    public static final String LABEL_WHEN = "When";
    public static final String LABEL_NO_MATCHES_FOUND = "No matches found.";
    public static final String LABEL_EXPLORE_MATCHES = "Explore Matches";
    public static final String LABEL_YOUR_MATCHES = "Your Matches";
    public static final String LABEL_HELLO_PREFIX = "Hello, ";
    public static final String LABEL_MATCH_PREFIX = "Match: ";

    // Icons
    public static final String ICON_CALENDAR = "📅 ";
    public static final String ICON_CLOCK = "🕒 ";
    public static final String ICON_PLAYERS = "👥";
    public static final String ICON_FOOTBALL = "⚽";
    public static final String ICON_BASKETBALL = "🏀";
    public static final String ICON_TENNIS = "🎾";
    public static final String ICON_PADEL = "🎾"; // Using tennis ball for now, or find a racket if possible, but tennis
                                                  // ball is standard
    public static final String ICON_EXTRAS_MEDAL = "🏅";

    // Bullet
    public static final String BULLET = " • ";

    // Additional CSS helpers used by home view
    public static final String CSS_TOGGLE_CONTAINER = "toggle-container";
    public static final String CSS_TOGGLE_BUTTON = "toggle-button";
    public static final String CSS_SEARCH_CAPSULE = "search-capsule";
    public static final String CSS_INTEGRATED_COMBO = "integrated-combo";
    public static final String CSS_INTEGRATED_DATE = "integrated-date";
    public static final String CSS_SEARCH_ACTION_BUTTON = "search-action-button";
    public static final String CSS_CARD_IMAGE_AREA = "card-image-area";
    public static final String CSS_CARD_PRICE_BADGE = "card-price-badge";
    public static final String CSS_CARD_CONTENT = "card-content";
    public static final String CSS_CARD_TITLE = "card-title";
    public static final String CSS_CARD_SUBTITLE = "card-subtitle";
    public static final String CSS_CARD_PROGRESS_BAR = "card-progress-bar";
    public static final String CSS_CARD_DETAIL_TEXT = "card-detail-text";
    public static final String CSS_SMALL = "small";

    // Localized messages
    public static final String ERROR_LOAD_HOME_VIEW_IT = "Impossibile caricare la home: ";

    // Default image resource paths; can be overridden via system properties:
    // -Dsporty.image.player=... and -Dsporty.image.organizer=...
    public static final String IMAGE_PLAYER_PATH = "/image/player.png";
    public static final String IMAGE_ORGANIZER_PATH = "/image/organizer.jpg";
    public static final String IMAGE_FOOTBALL_PATH = "/image/football.png";
    public static final String IMAGE_BASKETBALL_PATH = "/image/basketball.png";
    public static final String IMAGE_TENNIS_PATH = "/image/tennis.png";
    public static final String IMAGE_PADEL_PATH = "/image/padel.png";
    public static final String IMAGE_MEDAL_PATH = "/image/medal.png";

    // Small UI fragments
    public static final String LABEL_JOINED_SUFFIX = " joined";

    public static final String PADEL = "PADEL";
    public static final String FOOTBALL = "FOOTBALL";
    public static final String BASKET = "BASKET";
    public static final String TENNIS = "TENNIS";
}
