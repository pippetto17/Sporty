package exception;

/**
 * Exception thrown when a user attempts to perform an action
 * they are not authorized to perform.
 */
public class AuthorizationException extends Exception {

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
