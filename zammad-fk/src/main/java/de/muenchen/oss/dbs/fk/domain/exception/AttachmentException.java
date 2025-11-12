package de.muenchen.oss.dbs.fk.domain.exception;

@SuppressWarnings("PMD.MissingSerialVersionUID")
public class AttachmentException extends RuntimeException {
    public AttachmentException(final String message) {
        super(message);
    }

    public AttachmentException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
