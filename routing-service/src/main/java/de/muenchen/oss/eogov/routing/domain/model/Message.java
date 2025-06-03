package de.muenchen.oss.eogov.routing.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import lombok.Builder;

@Builder
public record Message(
        @NotBlank String id,
        @NotBlank String form,
        @NotBlank String formId,
        String caller,
        String client,
        String clientId,
        String customer,
        String customerId,
        String primaryDataAttachmentId,
        String primaryFormAttachmentId,
        String sender,
        String username,
        @NotNull ZonedDateTime timestamp,
        List<Attachment> attachments,
        Map<String, String> customParameters) {
}
