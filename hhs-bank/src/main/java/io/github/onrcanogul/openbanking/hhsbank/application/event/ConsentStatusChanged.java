package io.github.onrcanogul.openbanking.hhsbank.application.event;

import io.github.onrcanogul.openbanking.hhsbank.common.messaging.DomainEvent;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.Consent;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ConsentStatusChanged(
        String eventId,
        String consentId,
        String tppCode,
        String previousStatus,
        String newStatus,
        String cancellationReasonCode,
        OffsetDateTime occurredAt) implements DomainEvent {

    public static ConsentStatusChanged of(Consent before, Consent after, Clock clock) {
        return new ConsentStatusChanged(
                UUID.randomUUID().toString(),
                after.consentId(),
                after.tppCode(),
                before.status().ohvpsCode(),
                after.status().ohvpsCode(),
                after.cancellationReason() == null ? null : after.cancellationReason().code(),
                OffsetDateTime.now(clock));
    }
}