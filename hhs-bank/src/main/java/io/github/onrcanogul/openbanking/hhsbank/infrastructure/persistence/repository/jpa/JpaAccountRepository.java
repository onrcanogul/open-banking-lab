package io.github.onrcanogul.openbanking.hhsbank.infrastructure.persistence.repository.jpa;

import io.github.onrcanogul.openbanking.hhsbank.common.exception.InvalidSortFieldException;
import io.github.onrcanogul.openbanking.hhsbank.common.pagination.PageQuery;
import io.github.onrcanogul.openbanking.hhsbank.common.pagination.PageResult;
import io.github.onrcanogul.openbanking.hhsbank.common.pagination.SortDirection;
import io.github.onrcanogul.openbanking.hhsbank.domain.account.model.Account;
import io.github.onrcanogul.openbanking.hhsbank.domain.account.model.Balance;
import io.github.onrcanogul.openbanking.hhsbank.domain.account.repository.AccountRepository;
import io.github.onrcanogul.openbanking.hhsbank.infrastructure.persistence.entity.AccountEntity;
import io.github.onrcanogul.openbanking.hhsbank.infrastructure.persistence.entity.BalanceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class JpaAccountRepository implements AccountRepository {

    private static final Map<String, String> SORT_FIELDS = Map.of("hspRef", "accountRef");

    private final AccountJpaRepository accountRepository;
    private final BalanceJpaRepository balanceRepository;

    public JpaAccountRepository(AccountJpaRepository accountRepository, BalanceJpaRepository balanceRepository) {
        this.accountRepository = accountRepository;
        this.balanceRepository = balanceRepository;
    }

    @Override
    public Optional<Account> findByRef(String accountRef) {
        return accountRepository.findById(accountRef).map(AccountEntity::toDomain);
    }

    @Override
    public PageResult<Account> findByRefs(Set<String> accountRefs, String customerId, PageQuery page) {
        if(accountRefs.isEmpty()) {
            return new PageResult<>(List.of(), 0, page.pageNumber(), page.pageSize());
        }
        String field = Optional.ofNullable(SORT_FIELDS.get(page.sortField()))
                .orElseThrow(() -> new InvalidSortFieldException(page.sortField()));
        Sort.Direction dir = page.direction() == SortDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC;

        Page<AccountEntity> result = accountRepository.findByAccountRefInAndCustomerId(
                accountRefs, customerId, PageRequest.of(page.pageNumber() - 1, page.pageSize(), Sort.by(dir, field)));

        return new PageResult<>(
                result.getContent().stream().map(AccountEntity::toDomain).toList(),
                result.getTotalElements(), page.pageNumber(), page.pageSize());
    }

    @Override
    public Optional<Balance> findBalance(String accountRef) {
        return balanceRepository.findById(accountRef).map(BalanceEntity::toDomain);
    }

    @Override
    public List<Account> findByCustomer(String customerId) {
        return accountRepository.findByCustomerIdOrderByAccountRef(customerId).stream()
                .map(AccountEntity::toDomain).toList();
    }
}
