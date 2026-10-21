package io.github.onrcanogul.openbanking.hhsbank.application.consent;

import io.github.onrcanogul.openbanking.hhsbank.common.exception.ConsentNotActiveException;
import io.github.onrcanogul.openbanking.hhsbank.common.exception.ConsentNotFoundException;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.Consent;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.repository.ConsentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@Transactional(readOnly = true)
public class ConsentQueryService {
    private final ConsentRepository repository;
    private final Clock clock;

    public ConsentQueryService(ConsentRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public Consent getActive(String consentId) {
        Consent consent = repository.findById(consentId).orElseThrow(
                () -> new ConsentNotFoundException(consentId)
        );

        if(!consent.isUsable(clock)) {
            throw new ConsentNotActiveException(consentId, consent.status());
        }

        return consent;
    }

    public Consent getForTpp(String consentId, String tppCode) {
        return repository.findByConsentIdAndTppCode(consentId, tppCode).orElseThrow(
                () -> new ConsentNotFoundException(consentId)
        );
    }
}
