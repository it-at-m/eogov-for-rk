package de.muenchen.oss.eogov.routing.application.port.out;

import de.muenchen.oss.eogov.routing.domain.model.Message;

public interface SendMessageOutPort {
    void sendMessage(String destinationBinding, Message message);
}
