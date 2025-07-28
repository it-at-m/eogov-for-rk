package de.muenchen.oss.eogov.routing.adapter.out.s3;

@SuppressWarnings("PMD.MissingSerialVersionUID")
public class S3Exception extends RuntimeException {
    public S3Exception(final String message, final Throwable cause) {
        super(message, cause);
    }
}
