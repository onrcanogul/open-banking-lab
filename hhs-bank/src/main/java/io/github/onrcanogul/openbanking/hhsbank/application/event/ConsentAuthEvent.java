package io.github.onrcanogul.openbanking.hhsbank.application.event;

import java.time.OffsetDateTime;

public sealed interface ConsentAuthEvent permits GkdApproved, GkdRejected, ConsentRevokedByCustomer  {
    String eventId();
    String consentId();
    OffsetDateTime occurredAt();
}
