package de.muenchen.oss.eogov.routing.application.port.out;

import java.io.InputStream;

public interface PresignedUrlOutPort {
    String createFileAndCreatePresignedUrl(String path, InputStream content);
}
