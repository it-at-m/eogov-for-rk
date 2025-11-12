package de.muenchen.oss.dbs.fk.application.usecase.helper;

import de.muenchen.oss.dbs.fk.application.port.out.AttachmentOutPort;
import de.muenchen.oss.dbs.fk.application.port.out.TicketingOutPort;
import de.muenchen.oss.dbs.fk.config.DbsProperties;
import de.muenchen.oss.dbs.fk.domain.model.Attachment;
import de.muenchen.oss.dbs.fk.domain.model.Message;
import de.muenchen.oss.dbs.zammad.eai.ArticleAttachment;
import de.muenchen.oss.dbs.zammad.eai.CreateArticleDTO;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleHelper {
    public final static String CONTENT_TYPE_HTML = "text/html";
    private static final String ARTICLE_INTERNAL_ATTACHMENTS_TEXT = "Interner Artikel für interne Anhänge";

    private final DbsProperties dbsProperties;
    private final TicketingOutPort ticketingOutPort;
    private final AttachmentOutPort attachmentOutPort;

    public void createInternalAttachmentsArticle(final String ticketId, final Message message) {
        final CreateArticleDTO createArticleDTO = new CreateArticleDTO();
        createArticleDTO.setSubject(ARTICLE_INTERNAL_ATTACHMENTS_TEXT);
        createArticleDTO.setBody(ARTICLE_INTERNAL_ATTACHMENTS_TEXT);
        createArticleDTO.setType(CreateArticleDTO.TypeEnum.NOTE);
        createArticleDTO.setContentType(CONTENT_TYPE_HTML);
        createArticleDTO.setInternal(true);
        createArticleDTO.setAttachments(this.genInternalArticleAttachments(message));
        final String articleId = ticketingOutPort.createArticle(ticketId, createArticleDTO);
        log.info("Created internal article {} on ticket {}", articleId, ticketId);
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
                                    "Mime-Type", attachment.contentType()));
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
}
