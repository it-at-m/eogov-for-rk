package de.muenchen.oss.dbs.fk.adapter.in.streaming;

import de.muenchen.oss.dbs.fk.application.port.in.MessageInPort;
import de.muenchen.oss.dbs.fk.domain.model.Message;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreamingInAdapter {
    private final MessageInPort messageInPort;

    @Bean
    public Consumer<Message> message() {
        return message -> {
            try {
                log.info("Processing message: {}", message.id());
                messageInPort.process(message);
                log.info("Successfully processed message: {}", message.id());
            } catch (final Exception e) {
                log.error("Failed to process message: {}", message.id(), e);
                throw e;
            }
        };
    }
}
