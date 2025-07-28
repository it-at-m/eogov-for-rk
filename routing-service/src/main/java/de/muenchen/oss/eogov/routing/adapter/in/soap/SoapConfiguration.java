package de.muenchen.oss.eogov.routing.adapter.in.soap;

import de.cit.xmlns.intelliform._2009.webservices.backend.Application;
import jakarta.xml.ws.Endpoint;
import jakarta.xml.ws.soap.SOAPBinding;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SoapConfiguration {
    @Bean
    public Endpoint endpoint(final Bus bus, final Application application) {
        final EndpointImpl endpoint = new EndpointImpl(bus, application, SOAPBinding.SOAP11HTTP_MTOM_BINDING);
        endpoint.publish("/");
        return endpoint;
    }
}
