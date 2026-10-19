package io.github.onrcanogul.openbanking.hhsbank.infrastructure.persistence.repository.jpa;

import io.github.onrcanogul.openbanking.hhsbank.infrastructure.persistence.entity.AccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.Collection;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, String> {
    Page<AccountEntity> findByAccountRefInAndCustomerId(Collection<String> accountRefs, String customerId, PageRequest pageable);
}
