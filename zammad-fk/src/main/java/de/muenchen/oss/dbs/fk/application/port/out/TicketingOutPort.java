package de.muenchen.oss.dbs.fk.application.port.out;

import de.muenchen.oss.dbs.zammad.eai.CreateArticleDTO;
import de.muenchen.oss.dbs.zammad.eai.CreateTicketDTO;
import de.muenchen.oss.dbs.zammad.eai.CreateUserDTO;
import de.muenchen.oss.dbs.zammad.eai.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface TicketingOutPort {
    User getUserByLhmExtId(@NotBlank String lhmExtId);

    String createUser(@NotNull CreateUserDTO user);

    String createTicket(@NotNull CreateTicketDTO createTicketDTO, @NotBlank String userId);

    String createArticle(@NotBlank String ticketId, @NotNull CreateArticleDTO createArticleDTO);
}
