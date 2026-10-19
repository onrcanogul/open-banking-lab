package io.github.onrcanogul.openbanking.hhsbank.infrastructure.persistence.repository.jpa;

import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.Consent;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.repository.ConsentRepository;
import io.github.onrcanogul.openbanking.hhsbank.infrastructure.persistence.entity.ConsentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
class JpaConsentRepository implements ConsentRepository {

    private final ConsentJpaRepository consents;

    @Override
    public Optional<Consent> findById(String consentId) {
        return consents.findById(consentId).map(ConsentEntity::toDomain);
    }

    @Override
    public Consent save(Consent consent) {
        ConsentEntity entity = consents.findById(consent.consentId())
                .map(existing -> existing.apply(consent))
                .orElseGet(() -> ConsentEntity.fromDomain(consent));
        return consents.save(entity).toDomain();
    }
}