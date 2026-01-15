package exception;

/**
 * Eccezione custom per errori di accesso ai dati.
 * Sostituisce le generiche RuntimeException nel layer di persistenza.
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
