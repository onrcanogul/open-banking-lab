package io.github.onrcanogul.openbanking.hhsbank.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.onrcanogul.openbanking.hhsbank.domain.account.model.Balance;

public record BalanceResponse (
        @JsonProperty("hspRef") String accountRef,
        @JsonProperty("bakiyeTutar") String amount,
        @JsonProperty("prBrm") String currency
) {
    public static BalanceResponse from(Balance balance) {
        return new BalanceResponse(balance.accountRef(), balance.amount().toPlainString(), balance.Currency().name());
    }
}
