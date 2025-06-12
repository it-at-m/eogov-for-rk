package de.muenchen.oss.eogov.routing.adapter.in.soap;

import java.util.Collections;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter;
import org.springframework.ws.server.endpoint.adapter.method.MarshallingPayloadMethodProcessor;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;

@Configuration
public class SoapConfiguration {
    @Bean
    public ServletRegistrationBean<?> webServicesRegistration(final ApplicationContext ctx) {
        final MessageDispatcherServlet messageDispatcherServlet = new MessageDispatcherServlet();
        messageDispatcherServlet.setApplicationContext(ctx);
        messageDispatcherServlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(messageDispatcherServlet, "/api/soap", "*.wsdl");
    }

    @Bean
    public Jaxb2Marshaller marshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("de.cit.xmlns.intelliform._2009.webservices.backend");
        marshaller.setMtomEnabled(true);
        return marshaller;
    }

    @Bean
    public MarshallingPayloadMethodProcessor methodProcessor(final Jaxb2Marshaller marshaller) {
        return new MarshallingPayloadMethodProcessor(marshaller);
    }

    @Bean
    public DefaultMethodEndpointAdapter endpointAdapter(final MarshallingPayloadMethodProcessor methodProcessor) {
        final DefaultMethodEndpointAdapter adapter = new DefaultMethodEndpointAdapter();
        adapter.setMethodArgumentResolvers(Collections.singletonList(methodProcessor));
        adapter.setMethodReturnValueHandlers(Collections.singletonList(methodProcessor));
        return adapter;
    }

    @Bean
    public SimpleWsdl11Definition wsdl() {
        final SimpleWsdl11Definition definition = new SimpleWsdl11Definition();
        definition.setWsdl(new ClassPathResource("/wsdl/ApplicationService.wsdl"));
        return definition;
    }
}
