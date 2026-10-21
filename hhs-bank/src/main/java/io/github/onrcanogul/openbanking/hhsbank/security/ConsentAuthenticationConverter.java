package io.github.onrcanogul.openbanking.hhsbank.security;

import io.github.onrcanogul.openbanking.hhsbank.application.consent.ConsentContext;
import io.github.onrcanogul.openbanking.hhsbank.application.consent.ConsentQueryService;
import io.github.onrcanogul.openbanking.hhsbank.common.error.DomainErrorCodes;
import io.github.onrcanogul.openbanking.hhsbank.common.exception.ConsentNotActiveException;
import io.github.onrcanogul.openbanking.hhsbank.common.exception.ConsentNotFoundException;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.Consent;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConsentAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final ConsentQueryService consentQueryService;
    private final JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Consent consent;
        try {
            consent = consentQueryService.getActive(jwt.getClaimAsString("riza_no"));
        } catch (ConsentNotFoundException | ConsentNotActiveException e) {
            throw new OhvpsAuthenticationException(DomainErrorCodes.of(e).orElseThrow(), e);
        }

        ConsentContext ctx = new ConsentContext(
                consent.consentId(), consent.customerId(), consent.tppCode(),
                consent.permissions(), consent.accountRefs());

        return new ConsentAuthenticationToken(ctx, jwt, scopes.convert(jwt));
    }
}
