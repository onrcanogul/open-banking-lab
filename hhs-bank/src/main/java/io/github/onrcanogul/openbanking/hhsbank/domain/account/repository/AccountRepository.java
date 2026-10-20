package io.github.onrcanogul.openbanking.hhsbank.domain.account.repository;

import io.github.onrcanogul.openbanking.hhsbank.common.pagination.PageQuery;
import io.github.onrcanogul.openbanking.hhsbank.common.pagination.PageResult;
import io.github.onrcanogul.openbanking.hhsbank.domain.account.model.Account;
import io.github.onrcanogul.openbanking.hhsbank.domain.account.model.Balance;

import java.util.Optional;
import java.util.Set;

public interface AccountRepository {
    Optional<Account> findByRef(String accountRef);
    PageResult<Account> findByRefs(Set<String> accountRefs, String customerId, PageQuery page);
    Optional<Balance> findBalance(String accountRef);
}
