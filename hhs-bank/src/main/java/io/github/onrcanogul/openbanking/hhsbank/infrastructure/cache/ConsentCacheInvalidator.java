package io.github.onrcanogul.openbanking.hhsbank.infrastructure.cache;

import io.github.onrcanogul.openbanking.hhsbank.application.event.ConsentCacheInvalidation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
class ConsentCacheInvalidator {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void on(ConsentCacheInvalidation event) {
        // TODO: Redis evict
        log.debug("Consent cache invalidated: {}", event.consentId());
    }
}