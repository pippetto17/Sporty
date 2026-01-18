package exception;

public class InvalidCredentialsException extends ValidationException {
    public InvalidCredentialsException() {
        super("error.invalid.credentials");
    }
}

