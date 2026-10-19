package io.github.onrcanogul.openbanking.hhsbank.domain.account.model;

import io.github.onrcanogul.openbanking.hhsbank.common.enumeration.Currency;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record Balance(String accountRef, BigDecimal amount, Currency Currency, OffsetDateTime asOf) {
}
