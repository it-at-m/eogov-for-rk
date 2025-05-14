package de.muenchen.oss.eogov.routing.application.port.in;

import de.cit.xmlns.intelliform._2009.webservices.backend.DepositData;

public interface MessageDispatchInPort {
    void soapDeposit(DepositData data);
}
