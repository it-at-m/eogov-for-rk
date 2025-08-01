package de.muenchen.oss.dbs.fk.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public Optional<Attachment> getAttachment(final String id) {
        return this.attachments.stream()
                .filter(i -> i.id().equals(id))
                .findFirst();
    }
}
