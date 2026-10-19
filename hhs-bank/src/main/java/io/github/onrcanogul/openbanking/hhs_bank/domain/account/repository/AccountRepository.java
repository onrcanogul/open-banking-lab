package io.github.onrcanogul.openbanking.hhs_bank.domain.account.repository;

import io.github.onrcanogul.openbanking.hhs_bank.domain.account.model.Account;
import io.github.onrcanogul.openbanking.hhs_bank.domain.account.model.Balance;

import java.util.Optional;

public interface AccountRepository {
    Optional<Account> findByRef(String accountRef);
    Optional<Balance> findBalance(String accountRef);
}
