package de.muenchen.oss.dbs.fk.application.usecase.helper;

import de.muenchen.oss.dbs.fk.application.port.out.TicketingOutPort;
import de.muenchen.oss.dbs.fk.domain.model.Form;
import de.muenchen.oss.dbs.zammad.eai.CreateUserDTO;
import de.muenchen.oss.dbs.zammad.eai.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserHelper {
    private final TicketingOutPort ticketingOutPort;

    public String findOrCreateUser(@Valid final Form form) {
        // resolve user via lhmExtId
        if (StringUtils.isNotBlank(form.getLhmExtID())) {
            final Optional<String> existingUserId = findAndUpdateLhmExtIdUser(form);
            if (existingUserId.isPresent()) {
                return existingUserId.get();
            }
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

    private Optional<String> findAndUpdateLhmExtIdUser(final Form form) {
        final User user = ticketingOutPort.getUserByLhmExtId(form.getLhmExtID());
        if (user != null) {
            final String userId = user.getId();
            log.info("Using user {} identified by lhmExtId {}", userId, form.getLhmExtID());
            final boolean requiresUpdate = !form.getFirstname().equals(user.getFirstname()) || !form.getLastname().equals(user.getLastname());
            if (requiresUpdate) {
                // TODO do update
                log.warn("#findOrCreateUser existing user requires updated but skipped");
            }
            assert userId != null;
            return Optional.of(userId);
        }
        log.warn("No user found for given lhmExtId {}", form.getLhmExtID());
        return Optional.empty();
    }
}
