package de.muenchen.oss.dbs.fk.application.usecase.helper;

import de.muenchen.oss.dbs.fk.application.port.out.AttachmentOutPort;
import de.muenchen.oss.dbs.fk.application.port.out.TicketingOutPort;
import de.muenchen.oss.dbs.fk.config.DbsProperties;
import de.muenchen.oss.dbs.fk.domain.model.Attachment;
import de.muenchen.oss.dbs.fk.domain.model.Form;
import de.muenchen.oss.dbs.fk.domain.model.Message;
import de.muenchen.oss.dbs.zammad.eai.AttachmentDTO;
import de.muenchen.oss.dbs.zammad.eai.CreateTicketArticleDTO;
import de.muenchen.oss.dbs.zammad.eai.CreateTicketDTO;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketHelper {
    private final static String GROUP_USERS = "Users";
    private final static String ARTICLE_SUBJECT = "Inhalt des Anliegens";
    private final static String ARTICLE_TYPE_WEB = "web";
    private final static String SUMMARY_ATTACHMENT_ID = "summary";
    private static final String ARTICLE_DEFAULT_FALLBACK_BODY = "Die Zusammenfassung Ihres Anliegens konnte nicht geladen werden. " +
            "Die Daten sind dennoch erfolgreich bei uns eingegangen.";

    private final DbsProperties dbsProperties;
    private final TicketingOutPort ticketingOutPort;
    private final AttachmentOutPort attachmentOutPort;

    public String createTicket(final String userId, final Message message, final Form form) {
        final CreateTicketDTO request = new CreateTicketDTO();
        final String title;
        if (form.getTicketingZusatz() != null) {
            title = String.format("%s: %s", message.form(), form.getTicketingZusatz());
        } else {
            title = message.form();
        }
        request.setTitle(title);
        request.setGroup(GROUP_USERS);
        request.setAnliegenart(form.getTicketingAnliegenart());
        request.setVertrauensniveau(form.getTicketingVertrauensniveau());
        request.setDirektkennwort(this.genOnetimePassword());
        request.setVerwendeterIdpBeiTicketerstellung(form.getAccountSource());
        request.setAdditionalData(this.genAdditionalData(form));
        // construct article
        final CreateTicketArticleDTO ticketArticleDTO = new CreateTicketArticleDTO();
        ticketArticleDTO.setSubject(ARTICLE_SUBJECT);
        ticketArticleDTO.setType(ARTICLE_TYPE_WEB);
        ticketArticleDTO.setContentType(ArticleHelper.CONTENT_TYPE_HTML);
        ticketArticleDTO.setInternal(false);
        ticketArticleDTO.setBody(this.genSummaryArticleBody(message));
        ticketArticleDTO.setAttachments(this.genPublicTicketAttachments(message));
        request.setArticle(ticketArticleDTO);
        final String ticketId = ticketingOutPort.createTicket(request, userId);
        log.info("Created ticket {}", ticketId);
        return ticketId;
    }

    protected String genSummaryArticleBody(final Message message) {
        // TODO check getting by id ok, old was name
        final Optional<Attachment> summaryAttachment = message.getAttachment(SUMMARY_ATTACHMENT_ID);
        if (summaryAttachment.isPresent()) {
            try (InputStream inputStream = summaryAttachment.get().getInputStream(this.attachmentOutPort)) {
                return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            } catch (final IOException e) {
                log.error("Error while reading summary.html as body", e);
            }
        }
        return ARTICLE_DEFAULT_FALLBACK_BODY;
    }

    protected List<AttachmentDTO> genPublicTicketAttachments(final Message message) {
        final List<Attachment> attachments = message.attachments();
        return attachments.stream()
                // skip configured attachments
                .filter(i -> !dbsProperties.getSkipAttachments().contains(i.id()))
                // skip internal
                .filter(i -> !dbsProperties.getInternalAttachments().contains(i.id()))
                .map(
                        attachment -> {
                            final AttachmentDTO attachmentDTO = new AttachmentDTO();
                            attachmentDTO.setMimeType(attachment.contentType());
                            attachmentDTO.setFilename(attachment.name());
                            try (InputStream content = attachment.getInputStream(this.attachmentOutPort)) {
                                // FIXME streaming
                                final String attachmentBase64 = Base64.getEncoder().encodeToString(content.readAllBytes());
                                attachmentDTO.setData(attachmentBase64);
                            } catch (final IOException e) {
                                log.error("Error while converting attachment to b64 String", e);
                                return null;
                            }
                            return attachmentDTO;
                        })
                .filter(Objects::nonNull).toList();
    }

    protected Map<String, String> genAdditionalData(final Form form) {
        // TODO correct source?
        final Object additionalData = form.getInputs().get("dbsAdditionalData");
        if (additionalData == null) {
            return Map.of();
        }
        if (additionalData instanceof Map<?, ?> map) {
            return map.entrySet().stream()
                    .filter(entry -> {
                        if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
                            return true;
                        }
                        log.warn("#getAdditionalData skipping non-string entry: {}", entry);
                        return false;
                    })
                    .map(entry -> (Map.Entry<String, String>) entry)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        throw new IllegalStateException("#getAdditionalData dbs additional data in form is no valid Map");
    }

    protected String genOnetimePassword() {
        // TODO
        return null;
    }
}
