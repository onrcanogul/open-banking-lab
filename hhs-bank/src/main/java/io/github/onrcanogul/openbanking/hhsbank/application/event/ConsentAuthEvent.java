package io.github.onrcanogul.openbanking.hhsbank.application.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.OffsetDateTime;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = GkdApproved.class, name = "GkdApproved"),
        @JsonSubTypes.Type(value = GkdRejected.class, name = "GkdRejected"),
        @JsonSubTypes.Type(value = AccessTokenIssued.class, name = "AccessTokenIssued"),
        @JsonSubTypes.Type(value = ConsentRevokedByCustomer.class, name = "ConsentRevokedByCustomer")
})
public sealed interface ConsentAuthEvent
        permits AccessTokenIssued, ConsentRevokedByCustomer, GkdApproved, GkdRejected {
    String eventId();
    String consentId();
    OffsetDateTime occurredAt();
}
