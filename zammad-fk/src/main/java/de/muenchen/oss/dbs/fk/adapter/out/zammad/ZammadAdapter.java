package de.muenchen.oss.dbs.fk.adapter.out.zammad;

import de.muenchen.oss.dbs.fk.application.port.out.TicketingOutPort;
import de.muenchen.oss.dbs.fk.domain.exception.TicketingApiException;
import de.muenchen.oss.dbs.zammad.eai.Article;
import de.muenchen.oss.dbs.zammad.eai.ArticlesApi;
import de.muenchen.oss.dbs.zammad.eai.CreateArticleDTO;
import de.muenchen.oss.dbs.zammad.eai.CreateTicketDTO;
import de.muenchen.oss.dbs.zammad.eai.CreateUserDTO;
import de.muenchen.oss.dbs.zammad.eai.TicketInternal;
import de.muenchen.oss.dbs.zammad.eai.TicketsApi;
import de.muenchen.oss.dbs.zammad.eai.User;
import de.muenchen.oss.dbs.zammad.eai.UsersApi;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
public class ZammadAdapter implements TicketingOutPort {
    private final UsersApi usersApi;
    private final TicketsApi ticketsApi;
    private final ArticlesApi articlesApi;

    @Override
    public User getUserByLhmExtId(final String lhmExtId) {
        // TODO return null if not found
        try {
            return usersApi.getUserByLhmExtId(lhmExtId).block();
        } catch (final WebClientResponseException e) {
            final String message = String.format("Error while getUserByLhmExtId: %s", e.getResponseBodyAsString());
            throw new TicketingApiException(message, e);
        }
    }

    @Override
    public String createUser(final CreateUserDTO user) {
        try {
            final User response = usersApi.createUserWithLhmExtId(user, user.getLhmextid()).block();
            assert response != null;
            return response.getId();
        } catch (final WebClientResponseException e) {
            final String message = String.format("Error while createUser: %s", e.getResponseBodyAsString());
            throw new TicketingApiException(message, e);
        }
    }

    @Override
    public String createTicket(final CreateTicketDTO createTicketDTO, final String userId) {
        try {
            final TicketInternal response = ticketsApi.createNewTicket(createTicketDTO, null, userId).block();
            assert response != null;
            return response.getId();
        } catch (final WebClientResponseException e) {
            final String message = String.format("Error while createTicket: %s", e.getResponseBodyAsString());
            throw new TicketingApiException(message, e);
        }
    }

    @Override
    public String createArticle(final String ticketId, final CreateArticleDTO createArticleDTO) {
        try {
            final Article response = articlesApi.createNewArticle(ticketId, createArticleDTO, null, null, null).block();
            assert response != null;
            return response.getId();
        } catch (final WebClientResponseException e) {
            final String message = String.format("Error while createArticle: %s", e.getResponseBodyAsString());
            throw new TicketingApiException(message, e);
        }
    }
}
