package io.github.onrcanogul.openbanking.hhsbank.api.controller;

import io.github.onrcanogul.openbanking.hhsbank.api.model.AccountResponse;
import io.github.onrcanogul.openbanking.hhsbank.api.model.BalanceResponse;
import io.github.onrcanogul.openbanking.hhsbank.api.support.PageLinks;
import io.github.onrcanogul.openbanking.hhsbank.application.account.AccountQueryService;
import io.github.onrcanogul.openbanking.hhsbank.application.consent.ConsentContext;
import io.github.onrcanogul.openbanking.hhsbank.common.pagination.PageQuery;
import io.github.onrcanogul.openbanking.hhsbank.common.pagination.PageResult;
import io.github.onrcanogul.openbanking.hhsbank.common.pagination.SortDirection;
import io.github.onrcanogul.openbanking.hhsbank.domain.account.model.Account;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/ohvps/hbh/s2.0/hesaplar")
public class AccountController {

    private final AccountQueryService service;

    public AccountController(AccountQueryService service) {
        this.service = service;
    }

    @GetMapping("/{hspRef}/bakiye")
    public BalanceResponse balance(@AuthenticationPrincipal ConsentContext consentContext, @PathVariable("hspRef") String accountRef) {
        return BalanceResponse.from(service.getBalance(consentContext ,accountRef));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> list(
            @AuthenticationPrincipal ConsentContext consentContext,
            @RequestParam(name = "syfNo", defaultValue = "1") int pageNumber,
            @RequestParam(name = "syfKytSayi", defaultValue = "100") int pageSize,
            @RequestParam(name = "srlmKrtr", defaultValue = "hspRef") String sortField,
            @RequestParam(name = "srlmYon", defaultValue = "A") String sortDirection) {

        PageQuery page = new PageQuery(pageNumber, pageSize, sortField, SortDirection.fromOhvps(sortDirection));
        PageResult<Account> result = service.listAccounts(consentContext, page);

        List<AccountResponse> body = result.content().stream()
                .map(AccountResponse::from)
                .toList();

        return ResponseEntity.ok()
                .header("x-total-count", String.valueOf(result.totalCount()))
                .header(HttpHeaders.LINK, PageLinks.header(result, ServletUriComponentsBuilder.fromCurrentRequest()))
                .body(body);
    }

}
