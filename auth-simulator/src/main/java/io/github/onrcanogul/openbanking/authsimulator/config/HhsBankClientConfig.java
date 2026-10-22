package io.github.onrcanogul.openbanking.authsimulator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * hhs-bank's internal GKD API is called with this service's own client_credentials token (scope gkd_internal),
 * never with the customer's or the TPP's token — so the principal is fixed instead of taken from the request.
 */
@Configuration
class HhsBankClientConfig {

    static final String REGISTRATION_ID = "hhs-bank";
    private static final Authentication SERVICE_PRINCIPAL =
            UsernamePasswordAuthenticationToken.authenticated("auth-simulator", null, List.of());

    @Bean
    OAuth2AuthorizedClientManager serviceAuthorizedClientManager(ClientRegistrationRepository registrations,
                                                                 OAuth2AuthorizedClientService clients) {
        var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(registrations, clients);
        manager.setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build());
        return manager;
    }

    @Bean
    RestClient hhsBankRestClient(RestClient.Builder builder, OAuth2AuthorizedClientManager manager,
                                 SimulatorProperties props) {
        var oauth = new OAuth2ClientHttpRequestInterceptor(manager);
        oauth.setClientRegistrationIdResolver(request -> REGISTRATION_ID);
        oauth.setPrincipalResolver(request -> SERVICE_PRINCIPAL);
        return builder.baseUrl(props.hhsBankBaseUrl()).requestInterceptor(oauth).build();
    }
}
