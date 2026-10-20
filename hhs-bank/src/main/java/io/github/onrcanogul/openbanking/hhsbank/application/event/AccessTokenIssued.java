package io.github.onrcanogul.openbanking.hhsbank.application.event;

import java.time.OffsetDateTime;

public record AccessTokenIssued(String eventId, String consentId, OffsetDateTime occurredAt) implements ConsentAuthEvent {
}
