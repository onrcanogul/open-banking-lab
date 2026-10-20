package io.github.onrcanogul.openbanking.hhsbank.infrastructure.persistence.repository.jpa;

import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.Consent;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.ConsentStatus;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.repository.ConsentRepository;
import io.github.onrcanogul.openbanking.hhsbank.infrastructure.persistence.entity.ConsentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class JpaConsentRepository implements ConsentRepository {

    private final ConsentJpaRepository consentJpaRepository;

    @Override
    public Optional<Consent> findById(String consentId) {
        return consentJpaRepository.findById(consentId).map(ConsentEntity::toDomain);
    }

    @Override
    public Optional<Consent> findOpenConsent(String customerId, String tppCode) {
        List<ConsentEntity> open = consentJpaRepository.findByCustomerAndTppInStatuses(customerId, tppCode, ConsentStatus.openStatuses());
        if (open.size() > 1) {
            throw new IllegalStateException("Invariant violated: " + open.size() + " open consents for the same customer and TPP");
        }
        return open.stream().findFirst().map(ConsentEntity::toDomain);
    }

    @Override
    public Optional<Consent> findByConsentIdAndTppCode(String consentId, String tppCode) {
        return consentJpaRepository.findByConsentIdAndTppCode(consentId, tppCode).map(ConsentEntity::toDomain);
    }

    @Override
    public Consent save(Consent consent) {
        ConsentEntity entity = consentJpaRepository.findById(consent.consentId())
                .map(existing -> existing.apply(consent))
                .orElseGet(() -> ConsentEntity.fromDomain(consent));
        return consentJpaRepository.save(entity).toDomain();
    }

    @Override
    public Consent saveAndFlush(Consent consent) {
        ConsentEntity entity = consentJpaRepository.findById(consent.consentId())
                .map(existing -> existing.apply(consent))
                .orElseGet(() -> ConsentEntity.fromDomain(consent));
        return consentJpaRepository.saveAndFlush(entity).toDomain();
    }
}