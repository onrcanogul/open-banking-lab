package io.github.onrcanogul.openbanking.hhs_bank.domain.account.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record Balance(
        String accountRef,
        BigDecimal amount,
        String Currency, //TODO
        OffsetDateTime asOf
) {
}
