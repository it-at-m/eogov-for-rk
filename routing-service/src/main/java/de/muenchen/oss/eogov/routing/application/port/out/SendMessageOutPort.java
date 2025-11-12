package de.muenchen.oss.eogov.routing.application.port.out;

import de.muenchen.oss.eogov.routing.domain.model.Message;

public interface SendMessageOutPort {
    boolean destinationExists(String destinationBinding);

    void sendMessage(String destinationBinding, Message message);
}
