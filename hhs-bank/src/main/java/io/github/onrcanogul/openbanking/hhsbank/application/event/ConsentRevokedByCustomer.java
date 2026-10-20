package io.github.onrcanogul.openbanking.hhsbank.application.event;

import java.time.OffsetDateTime;

public record ConsentRevokedByCustomer(String eventId, String consentId, OffsetDateTime occurredAt, String channel
) implements ConsentAuthEvent {
}
