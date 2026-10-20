package io.github.onrcanogul.openbanking.hhsbank.application.consent;

import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.CustomerIdentity;

import java.util.Optional;

public interface CustomerDirectory {
    Optional<String> findCustomerId(CustomerIdentity identity);
}
