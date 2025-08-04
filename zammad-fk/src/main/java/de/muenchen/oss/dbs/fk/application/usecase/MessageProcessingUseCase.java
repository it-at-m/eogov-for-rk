package de.muenchen.oss.dbs.fk.application.usecase;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.muenchen.oss.dbs.fk.application.port.in.MessageInPort;
import de.muenchen.oss.dbs.fk.application.port.out.AttachmentOutPort;
import de.muenchen.oss.dbs.fk.application.usecase.helper.ArticleHelper;
import de.muenchen.oss.dbs.fk.application.usecase.helper.TicketHelper;
import de.muenchen.oss.dbs.fk.application.usecase.helper.UserHelper;
import de.muenchen.oss.dbs.fk.domain.exception.FormParsingException;
import de.muenchen.oss.dbs.fk.domain.model.Attachment;
import de.muenchen.oss.dbs.fk.domain.model.Form;
import de.muenchen.oss.dbs.fk.domain.model.Message;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageProcessingUseCase implements MessageInPort {
    private final UserHelper userHelper;
    private final TicketHelper ticketHelper;
    private final ArticleHelper articleHelper;
    private final XmlMapper xmlMapper = new XmlMapper();
    private final AttachmentOutPort attachmentOutPort;

    @Override
    public void process(final Message message) {
        // extract form content
        final Form form = this.getForm(message);
        // find or create user
        final String userId = userHelper.findOrCreateUser(form);
        // TODO find or create organisation
        // create ticket
        final String ticketId = ticketHelper.createTicket(userId, message, form);
        // create internal attachments article
        articleHelper.createInternalAttachmentsArticle(ticketId, message);
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
