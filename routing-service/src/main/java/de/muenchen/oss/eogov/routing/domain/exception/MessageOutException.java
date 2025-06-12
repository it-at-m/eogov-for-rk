package de.muenchen.oss.eogov.routing.domain.exception;

@SuppressWarnings("PMD.MissingSerialVersionUID")
public class MessageOutException extends RuntimeException {
    public MessageOutException(final String message) {
        super(message);
    }

    public MessageOutException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
