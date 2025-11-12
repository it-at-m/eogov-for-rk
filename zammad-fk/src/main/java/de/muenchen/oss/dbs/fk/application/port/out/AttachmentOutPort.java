package de.muenchen.oss.dbs.fk.application.port.out;

import java.io.InputStream;

public interface AttachmentOutPort {
    InputStream getPresignedUrlFile(String presignedUrl);
}
