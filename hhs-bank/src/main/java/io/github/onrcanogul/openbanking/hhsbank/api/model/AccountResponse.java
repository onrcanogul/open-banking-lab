package io.github.onrcanogul.openbanking.hhsbank.api.model;

import io.github.onrcanogul.openbanking.hhsbank.common.enumeration.Currency;
import io.github.onrcanogul.openbanking.hhsbank.domain.account.model.Account;

public record AccountResponse(String accountRef, String customerId, String iban, Currency currency) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(account.accountRef(), account.customerId(), account.iban(), account.currency());
    }
}
