package io.github.onrcanogul.openbanking.hhsbank.domain.consent.model;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Set;

public record Consent (
        String consentId,
        String customerId,
        String tppCode,
        ConsentStatus status,
        Set<ConsentPermission> permissions,
        Set<String> accountRefs,
        OffsetDateTime expiresAt
) {
    public boolean isActive(Clock clock) {
        return status == ConsentStatus.AUTHORIZED && OffsetDateTime.now(clock).isBefore(expiresAt);
    }
}
