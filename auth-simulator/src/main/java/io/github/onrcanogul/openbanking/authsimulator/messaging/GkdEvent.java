package io.github.onrcanogul.openbanking.authsimulator.messaging;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.OffsetDateTime;
import java.util.Set;

/**
 * Contract of the ohvps.gkd.events topic, consumed by hhs-bank (ConsentAuthEvent there).
 * Key = consentId, so every event of one consent lands on the same partition in publish order.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.SIMPLE_NAME, property = "eventType")
public sealed interface GkdEvent {
    String eventId();
    String consentId();
    OffsetDateTime occurredAt();

    record GkdApproved(String eventId, String consentId, OffsetDateTime occurredAt,
                       Set<String> selectedAccountRefs) implements GkdEvent {}

    record GkdRejected(String eventId, String consentId, OffsetDateTime occurredAt,
                       String cancellationReasonCode) implements GkdEvent {}

    record AccessTokenIssued(String eventId, String consentId, OffsetDateTime occurredAt) implements GkdEvent {}
}
