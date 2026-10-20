package io.github.onrcanogul.openbanking.hhsbank.domain.consent.model;

import io.github.onrcanogul.openbanking.hhsbank.common.exception.ConsentRevokedException;
import io.github.onrcanogul.openbanking.hhsbank.common.exception.InvalidConsentTransitionException;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Set;

import static io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.CancellationReason.TIMEOUT_AUTHORIZED;
import static io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.CancellationReason.TIMEOUT_AWAITING_AUTHORIZATION;
import static io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.ConsentStatus.*;

public record Consent (
        String consentId, String customerId, String tppCode,
        ConsentStatus status, CancellationReason cancellationReason,
        Set<ConsentPermission> permissions, Set<String> accountRefs,
        OffsetDateTime expiresAt, OffsetDateTime createdAt, OffsetDateTime updatedAt
) {
    public Consent authorize(Set<String> selectedAccountRefs, Clock clock) {
        requireStatus(ConsentStatus.AWAITING_AUTHORIZATION);
        if (selectedAccountRefs == null || selectedAccountRefs.isEmpty()) {
            throw new IllegalArgumentException("At least one account must be selected");
        }
        return with(ConsentStatus.AUTHORIZED, null, Set.copyOf(selectedAccountRefs), clock);
    }

    public Consent markUsed(Clock clock) {
        requireStatus(ConsentStatus.AUTHORIZED);
        return with(ConsentStatus.AUTHORIZATION_USED, null, accountRefs, clock);
    }

    public Consent revoke(CancellationReason reason, Clock clock) {
        if (reason != CancellationReason.CUSTOMER_VIA_HHS && reason != CancellationReason.CUSTOMER_VIA_TPP) {
            throw new IllegalArgumentException("Not a customer revocation reason: " + reason);
        }
        if (status.isFinal()) {
            throw new ConsentRevokedException(consentId, status);
        }
        return with(REVOKED, reason, accountRefs, clock);
    }

    public Consent rejectGkd(CancellationReason reason, Clock clock) {
        requireStatus(ConsentStatus.AWAITING_AUTHORIZATION);
        if (!reason.isGkdFailure()) {
            throw new IllegalArgumentException("Not a GKD failure reason: " + reason);
        }
        return with(ConsentStatus.REVOKED, reason, accountRefs, clock);
    }

    public Consent timeout(Clock clock) {
        return switch (status) {
            case AWAITING_AUTHORIZATION -> with(REVOKED, TIMEOUT_AWAITING_AUTHORIZATION, accountRefs, clock);
            case AUTHORIZED -> with(REVOKED, TIMEOUT_AUTHORIZED, accountRefs, clock);
            default -> throw new InvalidConsentTransitionException(consentId, status, null);
        };
    }

    public Consent terminate(Clock clock) {
        requireStatus(AUTHORIZATION_USED);
        return with(TERMINATED, null, accountRefs, clock);
    }

    public boolean isActive(Clock clock) {
        return status == ConsentStatus.AUTHORIZED && OffsetDateTime.now(clock).isBefore(expiresAt);
    }

    private void requireStatus(ConsentStatus expected) {
        if (status != expected) {
            throw new InvalidConsentTransitionException(consentId, status, expected);
        }
    }

    private Consent with(ConsentStatus newStatus, CancellationReason reason,
                         Set<String> refs, Clock clock) {
        return new Consent(consentId, customerId, tppCode, newStatus, reason,
                permissions, refs, expiresAt, createdAt, OffsetDateTime.now(clock));
    }
}
