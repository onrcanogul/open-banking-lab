package io.github.onrcanogul.openbanking.authsimulator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
class SecurityConfig {

    @Bean
    @Order(1)
    SecurityFilterChain publicChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/.well-known/jwks.json", "/actuator/health")
                .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                .build();
    }

    /** ÖHVPS endpoints YÖS calls server-to-server with its Keycloak client_credentials token. */
    @Bean
    @Order(2)
    SecurityFilterChain ohvpsChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/ohvps/**")
                .authorizeHttpRequests(a -> a.anyRequest().hasAuthority("SCOPE_hesap_bilgisi"))
                .oauth2ResourceServer(rs -> rs.jwt(j -> { }))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    /**
     * The customer's browser: Keycloak login (authorization code + PKCE). prompt=login makes every GKD a fresh
     * authentication even when the customer still has a Keycloak SSO session; the local session is dropped
     * once the customer approves or rejects.
     */
    @Bean
    @Order(3)
    SecurityFilterChain gkdChain(HttpSecurity http, ClientRegistrationRepository registrations) throws Exception {
        DefaultOAuth2AuthorizationRequestResolver resolver = new DefaultOAuth2AuthorizationRequestResolver(
                registrations, OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);
        resolver.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce()
                .andThen(b -> b.additionalParameters(p -> p.put("prompt", "login"))));

        return http
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/gkd", "/gkd/**").authenticated()
                        .anyRequest().denyAll())
                .oauth2Login(o -> o.authorizationEndpoint(e -> e.authorizationRequestResolver(resolver)))
                .build();
    }

    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("Europe/Istanbul"));
    }
}
