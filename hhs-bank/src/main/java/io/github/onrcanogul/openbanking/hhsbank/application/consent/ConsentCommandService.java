package io.github.onrcanogul.openbanking.hhsbank.application.consent;

import io.github.onrcanogul.openbanking.hhsbank.application.consent.model.CreateConsentCommand;
import io.github.onrcanogul.openbanking.hhsbank.application.event.ConsentCacheInvalidation;
import io.github.onrcanogul.openbanking.hhsbank.application.event.ConsentStatusChanged;
import io.github.onrcanogul.openbanking.hhsbank.common.exception.OhvpsErrorCode;
import io.github.onrcanogul.openbanking.hhsbank.common.exception.OhvpsException;
import io.github.onrcanogul.openbanking.hhsbank.common.messaging.OutboxWriter;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.CancellationReason;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.Consent;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.ConsentStatus;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.repository.ConsentRepository;
import io.github.onrcanogul.openbanking.hhsbank.infrastructure.properties.MessagingProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsentCommandService {

    private final ConsentRepository repository;
    private final CustomerDirectory customerRepository;
    private final ConsentRequestRules ruleService;
    private final OutboxWriter outboxWriter;
    private final ApplicationEventPublisher localEventPublisher;
    private final MessagingProperties messaging;
    private final Clock clock;

    @Transactional
    public Consent create(CreateConsentCommand cmd) {
        ruleService.validate(cmd);

        String customerId = customerRepository.findCustomerId(cmd.identity())
                .orElseThrow(() -> new OhvpsException(OhvpsErrorCode.CUSTOMER_NOT_FOUND, "Customer not found"));

        repository.findOpenConsent(customerId, cmd.tppCodeBody()).ifPresent(existing -> {
            switch (existing.status()) {
                case AWAITING_AUTHORIZATION -> {
                    Consent cancelled = existing.cancelForNewRequest(clock);
                    repository.saveAndFlush(cancelled);
                    recordChange(existing, cancelled);
                }
                case AUTHORIZED, AUTHORIZATION_USED -> throw new OhvpsException(
                        OhvpsErrorCode.CONSENT_ALREADY_EXISTS, "An active consent already exists");
                default -> { }
            }
        });

        Consent created = Consent.requestNew(
                UUID.randomUUID().toString(), customerId, cmd.tppCodeBody(), cmd.identity(),
                cmd.permissions(), cmd.expiresAt(), cmd.transactionWindow(),
                cmd.gkdMethod() == null ? "Y" : cmd.gkdMethod(), cmd.redirectUrl(), clock);

        repository.save(created);
        recordChange(null, created);
        return created;
    }

    @Transactional
    public void revokeByTpp(String consentId, String tppCode, Optional<String> tokenConsentId) {
        Consent consent = repository.findById(consentId)
                .filter(c -> c.tppCode().equalsIgnoreCase(tppCode))
                .orElseThrow(() -> new OhvpsException(OhvpsErrorCode.NOT_FOUND, "Consent not found"));

        if (consent.status().isFinal()) {
            throw new OhvpsException(OhvpsErrorCode.CONSENT_REVOKED, "Consent already revoked or terminated");
        }

        if (consent.status() == ConsentStatus.AUTHORIZATION_USED) {
            String tokenConsent = tokenConsentId.orElseThrow(
                    () -> new OhvpsException(OhvpsErrorCode.INVALID_TOKEN, "Access token required"));
            if (!tokenConsent.equals(consentId)) {
                throw new OhvpsException(OhvpsErrorCode.NOT_FOUND, "Consent not found");
            }
        }

        Consent revoked = consent.revoke(CancellationReason.CUSTOMER_VIA_TPP, clock);
        repository.save(revoked);
        recordChange(consent, revoked);
    }

    private void recordChange(Consent before, Consent after) {
        outboxWriter.append("Consent", after.consentId(), messaging.topics().consentStatusChanged(),
                ConsentStatusChanged.of(before, after, clock));
        localEventPublisher.publishEvent(new ConsentCacheInvalidation(after.consentId()));
    }
}
