package io.github.onrcanogul.openbanking.hhsbank.infrastructure.persistence.entity;

import io.github.onrcanogul.openbanking.hhsbank.common.enumeration.Currency;
import io.github.onrcanogul.openbanking.hhsbank.domain.account.model.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountEntity {
    @Id
    @Column(name = "account_ref", length = 40)
    private String accountRef;

    @Column(name = "customer_id", nullable = false, length = 40)
    private String customerId;

    @Column(name = "iban", nullable = false, length = 34)
    private String iban;

    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    public Account toDomain() {
        return new Account(accountRef, customerId, iban, currency);
    }
}
