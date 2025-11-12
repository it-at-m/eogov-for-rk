package de.muenchen.oss.dbs.fk.adapter.out.zammad;

import de.muenchen.oss.dbs.zammad.eai.ApiClient;
import de.muenchen.oss.dbs.zammad.eai.ArticlesApi;
import de.muenchen.oss.dbs.zammad.eai.TicketsApi;
import de.muenchen.oss.dbs.zammad.eai.UsersApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
class ZammadConfiguration {
    @Bean
    protected ApiClient apiClient(final ZammadProperties zammadProperties,
            final ClientRegistrationRepository clientRegistrationRepository,
            final OAuth2AuthorizedClientService authorizedClientService) {
        final ServletOAuth2AuthorizedClientExchangeFilterFunction oauth = new ServletOAuth2AuthorizedClientExchangeFilterFunction(
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService));
        oauth.setDefaultClientRegistrationId("zammad");
        final WebClient webClient = ApiClient.buildWebClientBuilder()
                .baseUrl(zammadProperties.getBaseUrl())
                .apply(oauth.oauth2Configuration())
                .build();
        return new ApiClient(webClient);
    }

    @Bean
    protected UsersApi usersApi(final ApiClient apiClient) {
        return new UsersApi(apiClient);
    }

    @Bean
    protected TicketsApi ticketsApi(final ApiClient apiClient) {
        return new TicketsApi(apiClient);
    }

    @Bean
    protected ArticlesApi articlesApi(final ApiClient apiClient) {
        return new ArticlesApi(apiClient);
    }
}
