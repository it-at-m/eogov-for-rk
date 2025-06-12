package de.muenchen.oss.eogov.routing.domain.exception;

@SuppressWarnings("PMD.MissingSerialVersionUID")
public class RoutingException extends RuntimeException {
    public RoutingException(final String message) {
        super(message);
    }
}
