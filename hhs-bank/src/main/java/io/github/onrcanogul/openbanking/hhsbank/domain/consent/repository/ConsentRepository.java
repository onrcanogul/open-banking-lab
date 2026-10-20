package io.github.onrcanogul.openbanking.hhsbank.domain.consent.repository;

import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.Consent;

import java.util.Optional;

public interface ConsentRepository {
    Optional<Consent> findById(String consentId);
    Optional<Consent> findOpenConsent(String customerId, String tppCode);
    Optional<Consent> findByConsentIdAndTppCode(String consentId, String tppCode);
    Consent save(Consent consent);
    Consent saveAndFlush(Consent consent);
}
