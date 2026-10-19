package io.github.onrcanogul.openbanking.hhsbank.application.account;

import io.github.onrcanogul.openbanking.hhsbank.application.consent.ConsentContext;
import io.github.onrcanogul.openbanking.hhsbank.common.exception.AccountNotFoundException;
import io.github.onrcanogul.openbanking.hhsbank.common.pagination.PageQuery;
import io.github.onrcanogul.openbanking.hhsbank.common.pagination.PageResult;
import io.github.onrcanogul.openbanking.hhsbank.domain.account.model.Account;
import io.github.onrcanogul.openbanking.hhsbank.domain.account.model.Balance;
import io.github.onrcanogul.openbanking.hhsbank.domain.account.repository.AccountRepository;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.ConsentPermission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AccountQueryService {

    private final AccountRepository accountRepository;

    public AccountQueryService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public PageResult<Account> listAccounts(ConsentContext ctx, PageQuery page) {
        ctx.requirePermission(ConsentPermission.ACCOUNT_BASIC);

        PageResult<Account> result = accountRepository.findByRefs(ctx.accountRefs(), ctx.customerId(), page);

        //TODO: Add auditor.
        return result;
    }

    public Balance getBalance(ConsentContext ctx, String accountRef) {
        ctx.requirePermission(ConsentPermission.BALANCE);
        ctx.requireAccount(accountRef);

        Account account = accountRepository.findByRef(accountRef)
                .filter(a -> a.customerId().equals(ctx.customerId()))
                .orElseThrow(() -> new AccountNotFoundException(accountRef));

        Balance balance = accountRepository.findBalance(account.accountRef())
                .orElseThrow(() -> new AccountNotFoundException(accountRef));

        //TODO: Add auditor.
        return balance;
    }
}
