package railway.service;

/**
 * Thrown when a business rule is violated (e.g. duplicate username,
 * insufficient seats, invalid credentials). Kept unchecked so calling
 * console-menu code can catch it once and display a friendly message,
 * while SQLExceptions still bubble up separately for infrastructure errors.
 */
public class ServiceException extends RuntimeException {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
