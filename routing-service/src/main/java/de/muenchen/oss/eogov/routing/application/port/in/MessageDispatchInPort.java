package de.muenchen.oss.eogov.routing.application.port.in;

import de.cit.xmlns.intelliform._2009.webservices.backend.DepositData;
import de.muenchen.oss.eogov.routing.domain.model.Message;

public interface MessageDispatchInPort {
    void soapDeposit(DepositData data);

    void deposit(Message message);
}
