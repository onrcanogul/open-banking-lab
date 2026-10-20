package io.github.onrcanogul.openbanking.hhsbank.application.event;

import java.time.OffsetDateTime;
import java.util.Set;

public record GkdApproved(String eventId, String consentId, OffsetDateTime occurredAt,
                          Set<String> selectedAccountRefs) implements ConsentAuthEvent {
}
