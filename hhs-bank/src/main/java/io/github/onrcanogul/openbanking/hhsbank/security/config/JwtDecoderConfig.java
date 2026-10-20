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
        return build(props, new JwtClaimValidator<String>("riza_no", Objects::nonNull));
    }

    @Bean
    @Qualifier("tppTokenDecoder")
    JwtDecoder tppTokenDecoder(OhvpsSecurityProperties props) {
        return build(props, new JwtClaimValidator<String>("azp", azp -> azp != null && !azp.isBlank()));
    }

    private JwtDecoder build(OhvpsSecurityProperties props, OAuth2TokenValidator<Jwt> specific) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(props.jwkSetUri())
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(props.clockSkew()),
                new JwtIssuerValidator(props.issuer()),
                new JwtClaimValidator<List<String>>(JwtClaimNames.AUD,
                        aud -> aud != null && aud.contains(props.audience())),
                specific));
        return decoder;
    }
}
