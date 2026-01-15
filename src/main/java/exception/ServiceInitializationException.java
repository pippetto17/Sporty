package exception;

/**
 * Eccezione lanciata quando si verifica un errore durante l'inizializzazione
 * dei service.
 */
public class ServiceInitializationException extends RuntimeException {

    public ServiceInitializationException(String message) {
        super(message);
    }

    public ServiceInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
