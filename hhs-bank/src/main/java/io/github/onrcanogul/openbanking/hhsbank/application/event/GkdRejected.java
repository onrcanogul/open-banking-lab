package io.github.onrcanogul.openbanking.hhsbank.application.event;

import java.time.OffsetDateTime;

public record GkdRejected(String eventId, String consentId, OffsetDateTime occurredAt,
                          String cancellationReasonCode) implements ConsentAuthEvent {
}
