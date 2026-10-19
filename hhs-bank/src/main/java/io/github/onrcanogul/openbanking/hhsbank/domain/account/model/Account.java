package io.github.onrcanogul.openbanking.hhsbank.domain.account.model;

import io.github.onrcanogul.openbanking.hhsbank.common.enumeration.Currency;

public record Account(String accountRef, String customerId, String iban, Currency currency) {
}
