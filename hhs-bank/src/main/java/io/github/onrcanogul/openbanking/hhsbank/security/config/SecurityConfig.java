package io.github.onrcanogul.openbanking.hhsbank.security.config;

import io.github.onrcanogul.openbanking.hhsbank.security.ConsentAuthenticationConverter;
import io.github.onrcanogul.openbanking.hhsbank.security.OhvpsSecurityErrorHandler;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
class SecurityConfig {

    private final ConsentAuthenticationConverter consentConverter;
    private final OhvpsSecurityErrorHandler errorHandler;

    @Bean
    @Order(1)
    SecurityFilterChain publicChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/actuator/health")
                .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                .build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain ohvpsChain(HttpSecurity http, JwtDecoder decoder) throws Exception {
        return http
                .securityMatcher("/ohvps/**")
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/ohvps/hbh/**").hasAuthority("SCOPE_hesap_bilgisi")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(rs -> rs
                        .bearerTokenResolver(req -> req.getHeader("X-Access-Token"))
                        .jwt(j -> j
                                .decoder(decoder)
                                .jwtAuthenticationConverter(consentConverter))
                        .authenticationEntryPoint(errorHandler))
                .exceptionHandling(e -> e.accessDeniedHandler(errorHandler))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    @Order(3)
    SecurityFilterChain denyEverythingElse(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(a -> a
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .anyRequest().denyAll())
                .build();
    }
}