package io.github.onrcanogul.openbanking.hhsbank.security;

import io.github.onrcanogul.openbanking.hhsbank.application.consent.ConsentContext;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

public class ConsentAuthenticationToken extends AbstractAuthenticationToken {
    private final ConsentContext consent;
    private final Jwt jwt;

    public ConsentAuthenticationToken(ConsentContext consent, Jwt jwt,
                                      Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.consent = consent;
        this.jwt = jwt;
        setAuthenticated(true);
    }

    @Override
    public @Nullable Object getCredentials() {
        return jwt;
    }

    @Override
    public @Nullable Object getPrincipal() {
        return consent;
    }
}
