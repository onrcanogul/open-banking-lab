package io.github.onrcanogul.openbanking.hhsbank.security;

import io.github.onrcanogul.openbanking.hhsbank.common.exception.OhvpsErrorCode;
import org.springframework.security.core.AuthenticationException;

/** Authentication failure that already knows which ÖHVPS error to answer with (e.g. ConsentRevoked, NotFound). */
public class OhvpsAuthenticationException extends AuthenticationException {

    private final OhvpsErrorCode errorCode;

    public OhvpsAuthenticationException(OhvpsErrorCode errorCode, Throwable cause) {
        super(errorCode.code(), cause);
        this.errorCode = errorCode;
    }

    public OhvpsErrorCode errorCode() {
        return errorCode;
    }
}
