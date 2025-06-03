package de.muenchen.oss.eogov.routing.application;

import de.cit.xmlns.intelliform._2009.webservices.backend.DepositData;
import de.cit.xmlns.intelliform._2009.webservices.backend.Entry;
import de.muenchen.oss.eogov.routing.application.port.in.MessageDispatchInPort;
import de.muenchen.oss.eogov.routing.application.port.out.PresignedUrlOutPort;
import de.muenchen.oss.eogov.routing.application.port.out.SendMessageOutPort;
import de.muenchen.oss.eogov.routing.domain.exception.AttachmentException;
import de.muenchen.oss.eogov.routing.domain.mapper.MessageMapper;
import de.muenchen.oss.eogov.routing.domain.model.Attachment;
import de.muenchen.oss.eogov.routing.domain.model.Message;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageDispatchUseCase implements MessageDispatchInPort {
    private static final String ROUTING_KEY = "for-kafka-routing-target";

    private final PresignedUrlOutPort presignedUrlOutPort;
    private final SendMessageOutPort sendMessageOutPort;
    private final MessageMapper messageMapper;

    @Override
    public void soapDeposit(final DepositData data) {
        log.info("Received message {} from form {}", data.getId(), data.getFormId());
        try {
            // resolve destination
            final String destinationBinding = this.resolveDestinationBinding(data);
            // map attachments
            final List<Attachment> attachments = this.mapAttachments(data.getId(), data.getAttachments());
            // map message
            final Message message = messageMapper.map(data, attachments);
            // send message
            this.sendMessageOutPort.sendMessage(destinationBinding, message);
        } catch (final IOException | MimeTypeException e) {
            // TODO
            throw new RuntimeException(e);
        }
        log.info("Message {} processed successfully", data.getId());
    }

    protected List<Attachment> mapAttachments(final String messageId, final List<de.cit.xmlns.intelliform._2009.webservices.backend.Attachment> attachments)
            throws IOException, MimeTypeException {
        final String pathPrefix = String.format("%s/%s",
                LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                messageId);
        return attachments.parallelStream().map(i -> this.mapAttachment(pathPrefix, i)).toList();
    }

    protected Attachment mapAttachment(final String pathPrefix, final de.cit.xmlns.intelliform._2009.webservices.backend.Attachment attachment) {
        try {
            // get file extension from content type
            final String extension = MimeTypes.getDefaultMimeTypes().forName(attachment.getContentType()).getExtension();
            // upload attachment and get presigned url
            // append UUID as attachment id isn't unique
            final String path = String.format("%s/%s_%s%s", pathPrefix, attachment.getId(), UUID.randomUUID(), extension);
            //final InputStream decodedContent = Base64.getDecoder().wrap(attachment.getContent().getInputStream());
            final String presignedUrl = presignedUrlOutPort.createFileAndCreatePresignedUrl(path, attachment.getContent().getInputStream());
            // map attachment
            return this.messageMapper.map(attachment, presignedUrl);
        } catch (final MimeTypeException | IOException e) {
            throw new AttachmentException("Mapping of Attachment failed.", e);
        }
    }

    protected String resolveDestinationBinding(final DepositData data) {
        return data.getCustomParameters().stream()
                .filter(
                        i -> ROUTING_KEY.equals(i.getKey()))
                .map(Entry::getValue)
                .findFirst()
                // TODO custom Exception
                .orElseThrow(() -> new IllegalStateException("Routing key not found in customParameters"));
    }
}
