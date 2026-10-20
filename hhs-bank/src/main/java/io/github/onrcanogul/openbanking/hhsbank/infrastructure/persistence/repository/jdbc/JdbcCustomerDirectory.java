package io.github.onrcanogul.openbanking.hhsbank.infrastructure.persistence.repository.jdbc;

import io.github.onrcanogul.openbanking.hhsbank.application.consent.CustomerDirectory;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.CustomerIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
class JdbcCustomerDirectory implements CustomerDirectory {

    private final JdbcTemplate jdbc;

    @Override
    public Optional<String> findCustomerId(CustomerIdentity identity) {
        return jdbc.query("""
                SELECT customer_id FROM customer_identity
                WHERE identity_type = ? AND identity_value = ? AND customer_type = ?
                """,
                        (rs, n) -> rs.getString("customer_id"),
                        identity.type(), identity.value(), identity.customerType())
                .stream().findFirst();
    }
}
