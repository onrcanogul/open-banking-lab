package io.github.onrcanogul.openbanking.hhsbank.application.account;

import io.github.onrcanogul.openbanking.hhsbank.application.audit.DataAccessAuditor;
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

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AccountQueryService {

    private final AccountRepository accountRepository;
    private final DataAccessAuditor auditorService;

    public AccountQueryService(AccountRepository accountRepository, DataAccessAuditor auditorService) {
        this.accountRepository = accountRepository;
        this.auditorService = auditorService;
    }

    public PageResult<Account> listAccounts(ConsentContext ctx, PageQuery page) {
        ctx.requirePermission(ConsentPermission.ACCOUNT_BASIC);

        PageResult<Account> result = accountRepository.findByRefs(ctx.accountRefs(), ctx.customerId(), page);

        auditorService.record(ctx, "ACCOUNT_LIST", result.content().stream().map(Account::accountRef).toList());
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

        auditorService.record(ctx, "BALANCE", List.of(accountRef));
        return balance;
    }
}
