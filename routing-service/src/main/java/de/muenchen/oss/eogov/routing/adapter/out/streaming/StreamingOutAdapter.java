package de.muenchen.oss.eogov.routing.adapter.out.streaming;

import de.muenchen.oss.eogov.routing.application.port.out.SendMessageOutPort;
import de.muenchen.oss.eogov.routing.domain.exception.MessageOutException;
import de.muenchen.oss.eogov.routing.domain.model.Message;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.binding.BindingService;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreamingOutAdapter implements SendMessageOutPort {
    private final BindingService bindingService;
    private final StreamBridge streamBridge;

    @Override
    public boolean destinationExists(final String destinationBinding) {
        return Arrays.asList(bindingService.getProducerBindingNames()).contains(destinationBinding);
    }

    @Override
    public void sendMessage(final String destinationBinding, final Message message) {
        final boolean successful;
        try {
            successful = streamBridge.send(destinationBinding, message);
        } catch (final RuntimeException e) {
            final String exceptionMessage = String.format("Exception while sending message with id %s.", message.id());
            throw new MessageOutException(exceptionMessage, e);
        }
        if (!successful) {
            final String exceptionMessage = String.format("Message with id %s couldn't be sent.", message.id());
            throw new MessageOutException(exceptionMessage);
        }
    }
}
