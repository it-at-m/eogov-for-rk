package de.muenchen.oss.eogov.routing.adapter.in.streaming;

import de.muenchen.oss.eogov.routing.application.port.in.MessageDispatchInPort;
import de.muenchen.oss.eogov.routing.domain.model.Message;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreamingInAdapter {
    private final MessageDispatchInPort messageDispatchInPort;

    @Bean
    public Consumer<Message> deposit() {
        return message -> {
            try {
                log.debug("Processing streaming message: {}", message.id());
                messageDispatchInPort.deposit(message);
                log.debug("Successfully processed message: {}", message.id());
            } catch (final Exception e) {
                log.error("Failed to process streaming message: {}", message.id(), e);
                throw e;
            }
        };
    }
}
