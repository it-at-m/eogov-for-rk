package de.muenchen.oss.eogov.routing.adapter.out.streaming;

import de.muenchen.oss.eogov.routing.application.port.out.SendMessageOutPort;
import de.muenchen.oss.eogov.routing.domain.exception.MessageOutException;
import de.muenchen.oss.eogov.routing.domain.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreamingAdapter implements SendMessageOutPort {
    private final StreamBridge streamBridge;

    @Override
    public void sendMessage(final String destinationBinding, final Message message) {
        try {
            final boolean successful = streamBridge.send(destinationBinding, message);
            if (!successful) {
                final String exceptionMessage = String.format("Message with id %s could be sent.", message.id());
                throw new MessageOutException(exceptionMessage);
            }
        } catch (final RuntimeException e) {
            final String exceptionMessage = String.format("Exception while sending message with id %s.", message.id());
            throw new MessageOutException(exceptionMessage, e);
        }
    }
}
