package io.github.onrcanogul.openbanking.hhsbank.infrastructure.persistence.repository.jpa;

import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.ConsentStatus;
import io.github.onrcanogul.openbanking.hhsbank.infrastructure.persistence.entity.ConsentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ConsentJpaRepository extends JpaRepository<ConsentEntity, String> {
    @Query("""
        select c from ConsentEntity c
        where c.customerId = :customerId
          and c.tppCode = :tppCode
          and c.status in :statuses
        order by c.createdAt desc
        """)
    List<ConsentEntity> findByCustomerAndTppInStatuses(
            @Param("customerId") String customerId,
            @Param("tppCode") String tppCode,
            @Param("statuses") Collection<ConsentStatus> statuses);

     Optional<ConsentEntity> findByConsentIdAndTppCode(
            String consentId,
            String tppCode
    );
}
