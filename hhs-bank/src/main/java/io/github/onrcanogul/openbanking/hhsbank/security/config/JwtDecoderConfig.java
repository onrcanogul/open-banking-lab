package io.github.onrcanogul.openbanking.hhsbank.security.config;

import io.github.onrcanogul.openbanking.hhsbank.security.OhvpsSecurityProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import java.util.List;
import java.util.Objects;

@Configuration
class JwtDecoderConfig {

    @Bean
    @Qualifier("customerTokenDecoder")
    JwtDecoder customerTokenDecoder(OhvpsSecurityProperties props) {
        return build(props, props.customerIssuer(), new JwtClaimValidator<String>("riza_no", Objects::nonNull));
    }

    /** TPP and internal service tokens (client_credentials); azp identifies the calling client. */
    @Bean
    @Qualifier("clientTokenDecoder")
    JwtDecoder clientTokenDecoder(OhvpsSecurityProperties props) {
        return build(props, props.clientIssuer(), new JwtClaimValidator<String>("azp", azp -> azp != null && !azp.isBlank()));
    }

    private JwtDecoder build(OhvpsSecurityProperties props, OhvpsSecurityProperties.Issuer issuer,
                             OAuth2TokenValidator<Jwt> specific) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(issuer.jwkSetUri())
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(props.clockSkew()),
                new JwtIssuerValidator(issuer.issuer()),
                new JwtClaimValidator<List<String>>(JwtClaimNames.AUD,
                        aud -> aud != null && aud.contains(props.audience())),
                specific));
        return decoder;
    }
}
