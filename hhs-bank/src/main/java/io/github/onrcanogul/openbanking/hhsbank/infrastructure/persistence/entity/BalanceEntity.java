package io.github.onrcanogul.openbanking.hhsbank.infrastructure.persistence.entity;

import io.github.onrcanogul.openbanking.hhsbank.common.enumeration.Currency;
import io.github.onrcanogul.openbanking.hhsbank.domain.account.model.Balance;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "account_balance")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BalanceEntity {
    @Id
    @Column(name = "account_ref", length = 40)
    private String accountRef;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    @Column(name = "as_of", nullable = false)
    private OffsetDateTime asOf;

    public Balance toDomain() {
        return new Balance(accountRef, amount, currency, asOf);
    }
}
