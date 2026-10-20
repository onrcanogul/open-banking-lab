package io.github.onrcanogul.openbanking.hhsbank.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

public final class TppAuthenticationToken extends AbstractAuthenticationToken {

    private final TppContext tpp;
    private final Jwt jwt;

    public TppAuthenticationToken(TppContext tpp, Jwt jwt, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.tpp = tpp;
        this.jwt = jwt;
        setAuthenticated(true);
    }

    @Override public TppContext getPrincipal() { return tpp; }
    @Override public Jwt getCredentials() { return jwt; }
}