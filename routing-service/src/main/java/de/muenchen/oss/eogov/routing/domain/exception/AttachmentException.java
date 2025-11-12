package de.muenchen.oss.eogov.routing.domain.exception;

@SuppressWarnings("PMD.MissingSerialVersionUID")
public class AttachmentException extends RuntimeException {
    public AttachmentException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
