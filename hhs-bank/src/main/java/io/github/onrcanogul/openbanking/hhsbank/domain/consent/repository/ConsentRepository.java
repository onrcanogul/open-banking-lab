package io.github.onrcanogul.openbanking.hhsbank.domain.consent.repository;

import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.Consent;

import java.util.Optional;

public interface ConsentRepository {
    Optional<Consent> findById(String consentId);
    Consent save(Consent consent);
}
