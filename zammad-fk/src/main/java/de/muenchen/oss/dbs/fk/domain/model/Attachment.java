package de.muenchen.oss.dbs.fk.domain.model;

import de.muenchen.oss.dbs.fk.application.port.out.AttachmentOutPort;
import jakarta.validation.constraints.NotBlank;
import java.io.InputStream;
import java.util.Map;
import lombok.Builder;

@Builder
public record Attachment(
        @NotBlank String id,
        @NotBlank String name,
        @NotBlank String contentType,
        Map<String, String> attributes,
        String presignedUrl) {
    public InputStream getInputStream(final AttachmentOutPort attachmentOutPort) {
        return attachmentOutPort.getPresignedUrlFile(this.presignedUrl);
    }
}
