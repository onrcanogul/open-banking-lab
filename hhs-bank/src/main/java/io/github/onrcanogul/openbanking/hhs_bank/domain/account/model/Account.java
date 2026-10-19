package io.github.onrcanogul.openbanking.hhs_bank.domain.account.model;

public record Account(
        String accountRef,
        String customerId,
        String iban,
        //TODO
        String currency ) {
}
