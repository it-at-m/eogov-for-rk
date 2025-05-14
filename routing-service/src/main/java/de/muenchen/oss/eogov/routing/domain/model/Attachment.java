package de.muenchen.oss.eogov.routing.domain.model;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.Builder;

@Builder
public record Attachment(
        @NotBlank String id,
        @NotBlank String name,
        @NotBlank String contentType,
        Map<String, String> attributes,
        String presignedUrl
) {
}
