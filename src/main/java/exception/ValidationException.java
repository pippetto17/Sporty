package exception;

public class ValidationException extends Exception {
    private final String messageKey;

    public ValidationException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
    }

    public ValidationException(String messageKey, Throwable cause) {
        super(messageKey, cause);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}