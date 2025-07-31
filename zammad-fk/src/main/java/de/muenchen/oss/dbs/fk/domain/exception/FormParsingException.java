package de.muenchen.oss.dbs.fk.domain.exception;

@SuppressWarnings("PMD.MissingSerialVersionUID")
public class FormParsingException extends RuntimeException {
    public FormParsingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
