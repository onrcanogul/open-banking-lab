package io.github.onrcanogul.openbanking.hhsbank.api.controller;

import io.github.onrcanogul.openbanking.hhsbank.api.model.AccountResponse;
import io.github.onrcanogul.openbanking.hhsbank.api.model.BalanceResponse;
import io.github.onrcanogul.openbanking.hhsbank.application.account.AccountQueryService;
import io.github.onrcanogul.openbanking.hhsbank.application.consent.ConsentContextResolver;
import io.github.onrcanogul.openbanking.hhsbank.common.pagination.PageQuery;
import io.github.onrcanogul.openbanking.hhsbank.common.pagination.PageResult;
import io.github.onrcanogul.openbanking.hhsbank.domain.account.model.Account;
import org.hibernate.query.SortDirection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ohvps/hbh&s2.0/hesaplar")
public class AccountController {

    private final AccountQueryService service;
    private final ConsentContextResolver consentContext;

    public AccountController(AccountQueryService service, ConsentContextResolver consentContext) {
        this.service = service;
        this.consentContext = consentContext;
    }

    @GetMapping("/{hspRef}/bakiye")
    BalanceResponse balance(@PathVariable("hspRef") String accountRef) {
        return BalanceResponse.from(service.getBalance(consentContext.current() ,accountRef));
    }

    @GetMapping
    ResponseEntity<List<AccountResponse>> list(
            @RequestParam(name = "syfNo", defaultValue = "1") int pageNumber,
            @RequestParam(name = "syfKytSayi", defaultValue = "25") int pageSize,
            @RequestParam(name = "srlmKrtr", defaultValue = "hspRef") String sortField,
            @RequestParam(name = "srlmYon", defaultValue = "A") String sortDirection) {

        PageQuery page = new PageQuery(pageNumber, pageSize, sortField, SortDirection.valueOf(sortDirection));
        PageResult<Account> result = service.listAccounts(consentContext.current(), page);

        List<AccountResponse> body = result.content().stream()
                .map(AccountResponse::from)
                .toList();

        return ResponseEntity.ok()
                .header("x-total-count", String.valueOf(result.totalCount()))
                .header(HttpHeaders.LINK)
                .body(body);
    }

}
