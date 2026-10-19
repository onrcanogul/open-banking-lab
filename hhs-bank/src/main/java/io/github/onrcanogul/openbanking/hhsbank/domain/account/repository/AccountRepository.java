package io.github.onrcanogul.openbanking.hhsbank.domain.account.repository;

import io.github.onrcanogul.openbanking.hhsbank.domain.account.model.Account;
import io.github.onrcanogul.openbanking.hhsbank.domain.account.model.Balance;

import java.util.Optional;

public interface AccountRepository {
    Optional<Account> findByRef(String accountRef);
    Optional<Balance> findBalance(String accountRef);
}
