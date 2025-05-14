package de.muenchen.oss.eogov.routing.adapter.in.soap;

import de.cit.xmlns.intelliform._2009.webservices.backend.Deposit;
import de.muenchen.oss.eogov.routing.application.port.in.MessageDispatchInPort;
import jakarta.xml.bind.JAXBElement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;

@Endpoint
@RequiredArgsConstructor
@Slf4j
public class SoapAdapter {
    private static final String NAMESPACE_URI = "http://xmlns.cit.de/intelliform/2009/webservices/backend";

    private final MessageDispatchInPort messageDispatchInPort;

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "deposit")
    public void deposit(@RequestPayload final JAXBElement<Deposit> message) {
        log.info("test");
        //final DepositData data = deposit.getValue().getData();
        //messageDispatchInPort.soapDeposit(data);
    }
}
