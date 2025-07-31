package de.muenchen.oss.dbs.fk.application.port.in;

import de.muenchen.oss.dbs.fk.domain.model.Message;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface MessageInPort {
    void process(@NotNull @Valid Message message);
}
