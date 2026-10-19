package io.github.onrcanogul.openbanking.hhsbank.application.consent;

import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.Consent;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import org.springframework.security.oauth2.jwt.Jwt;
@Component
public class ConsentContextResolver {

    private final ConsentQueryService service;

    public ConsentContextResolver(ConsentQueryService service) {
        this.service = service;
    }

    public ConsentContext current() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Consent consent = service.getActive(jwt.getClaimAsString("riza_no"));

        return new ConsentContext(
                consent.consentId(), consent.customerId(), consent.tppCode(),
                consent.permissions(), consent.accountRefs()
        );
    }
}
