package io.github.onrcanogul.openbanking.hhsbank.common.exception;

import org.springframework.security.core.AuthenticationException;

public class ConsentNotActiveAuthenticationException extends AuthenticationException {
    public ConsentNotActiveAuthenticationException(Throwable cause) {
        super("Consent is not active", cause);
    }
}
