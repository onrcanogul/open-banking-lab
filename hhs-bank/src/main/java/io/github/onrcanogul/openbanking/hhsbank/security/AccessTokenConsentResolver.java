package io.github.onrcanogul.openbanking.hhsbank.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccessTokenConsentResolver {

    private final JwtDecoder decoder;

    public Optional<String> consentIdOf(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(decoder.decode(accessToken).getClaimAsString("riza_no"));
        } catch (JwtException e) {
            return Optional.empty();
        }
    }
}