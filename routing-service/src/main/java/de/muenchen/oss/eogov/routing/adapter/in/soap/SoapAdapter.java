package de.muenchen.oss.eogov.routing.adapter.in.soap;

import de.cit.xmlns.intelliform._2009.webservices.backend.*;
import de.muenchen.oss.eogov.routing.application.port.in.MessageDispatchInPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SoapAdapter implements Application {
    private final MessageDispatchInPort messageDispatchInPort;

    @Override
    public PrefillResult prefill(PrefillData data) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public DepositResult deposit(final DepositData data) throws ApplicationFault_Exception {
        try {
            messageDispatchInPort.soapDeposit(data);
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            final ApplicationFault fault = new ApplicationFault();
            fault.setMessage(e.getMessage());
            // TODO
            fault.setUserMessage("");
            throw new ApplicationFault_Exception(e.getMessage(), fault, e);
        }
        return null;
    }
}
