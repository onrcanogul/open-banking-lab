package io.github.onrcanogul.openbanking.hhsbank.application.consent;

import io.github.onrcanogul.openbanking.hhsbank.application.event.*;
import io.github.onrcanogul.openbanking.hhsbank.common.exception.ConsentNotFoundException;
import io.github.onrcanogul.openbanking.hhsbank.common.exception.InvalidConsentTransitionException;
import io.github.onrcanogul.openbanking.hhsbank.common.messaging.JdbcInbox;
import io.github.onrcanogul.openbanking.hhsbank.common.messaging.OutboxWriter;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.CancellationReason;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.Consent;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.repository.ConsentRepository;
import io.github.onrcanogul.openbanking.hhsbank.infrastructure.properties.MessagingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsentAuthEventHandler {

    private final JdbcInbox inbox;
    private final ConsentRepository repository;
    private final OutboxWriter outboxWriter;
    private final ApplicationEventPublisher localEventPublisher;
    private final MessagingProperties messaging;
    private final Clock clock;

    @Transactional
    public void handle(ConsentAuthEvent event, String consumer) {
        if(!inbox.tryRecord(consumer, event.eventId(), event.getClass().getSimpleName())) {
            log.info("Duplicate event skipped: {}", event.eventId());
            return;
        }
        Consent before = repository.findById(event.consentId())
                .orElseThrow(() -> new ConsentNotFoundException(event.consentId()));
        Consent after;
        try {
            after = apply(before, event);
        } catch (InvalidConsentTransitionException e) {
            if (before.status().isFinal()) {
                log.warn("Stale event ignored: {} for consent {} in status {}",
                        event.getClass().getSimpleName(), before.consentId(), before.status());
                return;
            }
            throw e;
        }
        repository.save(after);
        outboxWriter.append("Consent", after.consentId(), messaging.topics().consentStatusChanged(),
                ConsentStatusChanged.of(before, after, clock));
        localEventPublisher.publishEvent(new ConsentCacheInvalidation(after.consentId()));
    }

    private Consent apply(Consent consent, ConsentAuthEvent event) {
        return switch (event) {
            case GkdApproved e              -> consent.authorize(e.selectedAccountRefs(), clock);
            case GkdRejected e              -> consent.rejectGkd(CancellationReason.fromCode(e.cancellationReasonCode()), clock);
            case AccessTokenIssued e        -> consent.markUsed(clock);
            case ConsentRevokedByCustomer e -> consent.revoke(CancellationReason.CUSTOMER_VIA_HHS, clock);
        };
    }
}
