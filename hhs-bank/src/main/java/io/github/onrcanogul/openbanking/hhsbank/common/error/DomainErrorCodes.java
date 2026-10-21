package io.github.onrcanogul.openbanking.hhsbank.common.error;

import io.github.onrcanogul.openbanking.hhsbank.common.exception.*;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.ConsentStatus;
import org.springframework.http.HttpStatusCode;

import java.util.Optional;

import static io.github.onrcanogul.openbanking.hhsbank.common.exception.OhvpsErrorCode.*;

/**
 * Maps domain exceptions to ÖHVPS error codes so that domain code stays free of API concerns.
 * ConsentRevoked / ConsentMismatch / PermissionTypeNotSupported have no explicit HTTP status in the spec;
 * they are 403 in {@link OhvpsErrorCode} until better evidence is found.
 */
public final class DomainErrorCodes {

    private DomainErrorCodes() {}

    public static Optional<OhvpsErrorCode> of(Throwable ex) {
        return Optional.ofNullable(switch (ex) {
            case OhvpsException e -> e.errorCode();
            case ConsentNotFoundException e -> NOT_FOUND;
            case AccountNotFoundException e -> NOT_FOUND;
            case AccountNotInConsentException e -> NOT_FOUND;
            case ConsentPermissionException e -> PERMISSION_TYPE_NOT_SUPPORTED;
            case ConsentRevokedException e -> CONSENT_REVOKED;
            case ConsentNotActiveException e -> forInactiveConsent(e.status());
            case InvalidConsentTransitionException e -> CONSENT_MISMATCH;
            case InvalidPagingException e -> INVALID_FORMAT;
            case InvalidSortFieldException e -> INVALID_FORMAT;
            default -> null;
        });
    }

    /**
     * Cancelled/terminated consents (and an authorized one whose access expiry passed) are "revoked";
     * a consent still waiting for authorization or for its first token is a status mismatch.
     */
    public static OhvpsErrorCode forInactiveConsent(ConsentStatus status) {
        return switch (status) {
            case AWAITING_AUTHORIZATION, AUTHORIZED -> CONSENT_MISMATCH;
            case AUTHORIZATION_USED, TERMINATED, REVOKED -> CONSENT_REVOKED;
        };
    }

    /** Fallback for failures that only carry an HTTP status (container errors, Spring MVC exceptions). */
    public static OhvpsErrorCode forStatus(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> INVALID_FORMAT;
            case 401 -> INVALID_TOKEN;
            case 403 -> FORBIDDEN;
            case 404 -> NOT_FOUND;
            case 405 -> METHOD_NOT_ALLOWED;
            case 406 -> NOT_ACCEPTABLE;
            case 415 -> UNSUPPORTED_MEDIA_TYPE;
            case 429 -> EXCEEDED_RATE;
            case 503 -> SERVICE_UNAVAILABLE;
            default -> status.is4xxClientError() ? INVALID_FORMAT : INTERNAL_ERROR;
        };
    }
}
