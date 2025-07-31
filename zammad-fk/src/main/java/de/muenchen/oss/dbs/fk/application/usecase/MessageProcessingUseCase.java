package de.muenchen.oss.dbs.fk.application.usecase;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.muenchen.oss.dbs.fk.application.port.in.MessageInPort;
import de.muenchen.oss.dbs.fk.application.port.out.AttachmentOutPort;
import de.muenchen.oss.dbs.fk.application.port.out.TicketingOutPort;
import de.muenchen.oss.dbs.fk.config.DbsProperties;
import de.muenchen.oss.dbs.fk.domain.exception.FormParsingException;
import de.muenchen.oss.dbs.fk.domain.model.Attachment;
import de.muenchen.oss.dbs.fk.domain.model.Form;
import de.muenchen.oss.dbs.fk.domain.model.Message;
import de.muenchen.oss.dbs.zammad.eai.ArticleAttachment;
import de.muenchen.oss.dbs.zammad.eai.AttachmentDTO;
import de.muenchen.oss.dbs.zammad.eai.CreateArticleDTO;
import de.muenchen.oss.dbs.zammad.eai.CreateTicketArticleDTO;
import de.muenchen.oss.dbs.zammad.eai.CreateTicketDTO;
import de.muenchen.oss.dbs.zammad.eai.CreateUserDTO;
import de.muenchen.oss.dbs.zammad.eai.User;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageProcessingUseCase implements MessageInPort {
    private final static String GROUP_USERS = "Users";
    private final static String ARTICLE_SUBJECT = "Inhalt des Anliegens";
    private final static String ARTICLE_TYPE_WEB = "web";
    private final static String CONTENT_TYPE_HTML = "text/html";
    private final static String SUMMARY_ATTACHMENT_ID = "summary";
    private static final String ARTICLE_DEFAULT_FALLBACK_BODY = "Die Zusammenfassung Ihres Anliegens konnte nicht geladen werden." +
            "Die Daten sind dennoch erfolgreich bei uns eingegangen.";
    public static final String ARTICLE_INTERNAL_ATTACHMENTS_TEXT = "Interner Artikel für interne Anhänge";

    private final DbsProperties dbsProperties;
    private final XmlMapper xmlMapper = new XmlMapper();
    private final AttachmentOutPort attachmentOutPort;
    private final TicketingOutPort ticketingOutPort;

    @Override
    public void process(final Message message) {
        // extract form content
        final Form form = this.getForm(message);
        // find or create user
        final String userId = this.findOrCreateUser(form);
        // find or create organisation
        final String organisationId = this.findOrCreateOrganisation(form);
        // create ticket
        final String ticketId = this.createTicket(userId, message, form);
        // create internal attachments article
        this.createInternalAttachmentsArticle(ticketId, message);
    }

    protected String findOrCreateUser(@Valid final Form form) {
        // resolve user via lhmExtId
        if (StringUtils.isNotBlank(form.getLhmExtID())) {
            final User user = ticketingOutPort.getUserByLhmExtId(form.getLhmExtID());
            if (user != null) {
                final String userId = user.getId();
                log.info("Using user {} identified by lhmExtId {}", userId, form.getLhmExtID());
                final boolean requiresUpdate = !form.getFirstname().equals(user.getFirstname()) || !form.getLastname().equals(user.getLastname());
                // TODO do update
                return userId;
            }
            log.warn("No user found for given lhmExtId {}", form.getLhmExtID());
        }
        // create user
        final CreateUserDTO createUserDTO = new CreateUserDTO();
        createUserDTO.setFirstname(form.getFirstname());
        final String lastname = form.getLastname();
        if (StringUtils.isNotBlank(lastname)) {
            createUserDTO.setLastname(lastname);
        } else {
            // to prevent Zammad from extracting it from mail
            createUserDTO.setLastname("-");
        }
        createUserDTO.setEmail(form.getMail());
        if (StringUtils.isNotBlank(form.getLhmExtID())) {
            createUserDTO.setLhmextid(form.getLhmExtID());
        }
        final String userId = ticketingOutPort.createUser(createUserDTO);
        log.info("Created user {}", userId);
        return userId;
    }

    protected String findOrCreateOrganisation(final Form form) {
        if (StringUtils.isBlank(form.getPseudonymId())) {
            return null;
        }
        // TODO
        return "";
    }

    protected String createTicket(final String userId, final Message message, final Form form) {
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
        ticketArticleDTO.setContentType(CONTENT_TYPE_HTML);
        ticketArticleDTO.setInternal(false);
        ticketArticleDTO.setBody(this.genArticleBody(message));
        ticketArticleDTO.setAttachments(this.genPublicTicketAttachments(message));
        request.setArticle(ticketArticleDTO);
        final String ticketId = ticketingOutPort.createTicket(request, userId);
        log.info("Created ticket {}", ticketId);
        return ticketId;
    }

    protected void createInternalAttachmentsArticle(final String ticketId, final Message message) {
        final CreateArticleDTO createArticleDTO = new CreateArticleDTO();
        createArticleDTO.setSubject(ARTICLE_INTERNAL_ATTACHMENTS_TEXT);
        createArticleDTO.setBody(ARTICLE_INTERNAL_ATTACHMENTS_TEXT);
        createArticleDTO.setType(CreateArticleDTO.TypeEnum.NOTE);
        createArticleDTO.setContentType(CONTENT_TYPE_HTML);
        createArticleDTO.setInternal(true);
        // FIXME 422
        createArticleDTO.setAttachments(this.genInternalArticleAttachments(message));
        final String articleId = ticketingOutPort.createArticle(ticketId, createArticleDTO);
        log.info("Created internal article {} on ticket {}", articleId, ticketId);
    }

    protected String genArticleBody(final Message message) {
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

    protected List<ArticleAttachment> genInternalArticleAttachments(final Message message) {
        final List<Attachment> attachments = message.attachments();
        return attachments.stream()
                // skip configured attachments
                .filter(i -> !dbsProperties.getSkipAttachments().contains(i.id()))
                // filter for internal
                .filter(i -> dbsProperties.getInternalAttachments().contains(i.id()))
                .map(
                        attachment -> {
                            final ArticleAttachment attachmentDTO = new ArticleAttachment();
                            attachmentDTO.setMimeType(attachment.contentType());
                            attachmentDTO.setFilename(attachment.name());
                            attachmentDTO.setPreferences(Map.of(
                                    "Mime-Type", attachment.contentType()
                            ));
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
        if (additionalData instanceof Map) {
            return (Map<String, String>) additionalData;
        }
        if (additionalData == null) {
            return null;
        }
        throw new IllegalStateException("Dbs additional data in form is no valid Map");
    }

    protected String genOnetimePassword() {
        // TODO
        return null;
    }

    protected Form getForm(final Message message) {
        // https://git.muenchen.de/digitalisierung/dbs-ticketing/xslt-formularserver-nach-zammad/-/blob/main/Output/XML-Daten-EOZF-Antrag-angemeldet_Ergebnis.xml?ref_type=heads
        // TODO really data and not form
        final Attachment form = message.getAttachment(message.primaryDataAttachmentId())
                .orElseThrow(() -> new IllegalStateException("No attachment with primary form id"));
        try (InputStream formStream = form.getInputStream(this.attachmentOutPort)) {
            return xmlMapper.readValue(formStream, Form.class);
        } catch (final IOException e) {
            throw new FormParsingException("Error while parsing form xml", e);
        }
    }
}
