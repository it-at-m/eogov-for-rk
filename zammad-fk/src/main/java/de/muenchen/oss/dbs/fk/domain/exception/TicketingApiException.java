package de.muenchen.oss.dbs.fk.domain.exception;

@SuppressWarnings("PMD.MissingSerialVersionUID")
public class TicketingApiException extends RuntimeException {
    public TicketingApiException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
