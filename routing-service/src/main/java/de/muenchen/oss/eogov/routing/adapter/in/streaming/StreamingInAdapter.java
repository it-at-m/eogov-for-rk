package de.muenchen.oss.eogov.routing.adapter.in.streaming;

import de.muenchen.oss.eogov.routing.application.port.in.MessageDispatchInPort;
import de.muenchen.oss.eogov.routing.domain.model.Message;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StreamingInAdapter {
    private final MessageDispatchInPort messageDispatchInPort;

    @Bean
    public Consumer<Message> messageIn() {
        return messageDispatchInPort::deposit;
    }
}
