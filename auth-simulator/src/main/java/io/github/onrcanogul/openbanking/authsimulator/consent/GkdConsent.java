package io.github.onrcanogul.openbanking.authsimulator.consent;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** hhs-bank's internal view of a consent for the GKD channel (GET /internal/gkd/riza/{rizaNo}). */
public record GkdConsent(
        String consentId,
        String status,
        String tppCode,
        String customerId,
        Identity identity,
        List<String> permissions,
        OffsetDateTime expiresAt,
        OffsetDateTime authorizationDeadline,
        String redirectUrl,
        List<Account> accounts) {

    public static final String AWAITING_AUTHORIZATION = "B";
    public static final String AUTHORIZED = "Y";
    public static final String AUTHORIZATION_USED = "K";

    public record Identity(String type, String value, String customerType) {}

    public record Account(String accountRef, String iban, String currency) {}

    public boolean isAwaitingAuthorization() {
        return AWAITING_AUTHORIZATION.equals(status);
    }

    public boolean belongsTo(String identityValue) {
        return identity != null && identity.value() != null && identity.value().equals(identityValue);
    }

    public boolean isAuthorizationExpired(OffsetDateTime now) {
        return authorizationDeadline != null && !now.isBefore(authorizationDeadline);
    }

    public Set<String> accountRefs() {
        return accounts.stream().map(Account::accountRef).collect(Collectors.toUnmodifiableSet());
    }
}
