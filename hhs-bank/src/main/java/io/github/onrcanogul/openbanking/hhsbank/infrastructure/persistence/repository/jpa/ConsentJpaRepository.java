package io.github.onrcanogul.openbanking.hhsbank.infrastructure.persistence.repository.jpa;

import io.github.onrcanogul.openbanking.hhsbank.infrastructure.persistence.entity.ConsentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsentJpaRepository extends JpaRepository<ConsentEntity, String> {
}
