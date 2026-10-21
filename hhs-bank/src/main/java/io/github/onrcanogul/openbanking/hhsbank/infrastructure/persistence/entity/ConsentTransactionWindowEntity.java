package io.github.onrcanogul.openbanking.hhsbank.infrastructure.persistence.entity;

import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.TransactionWindow;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "consent_transaction_window")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsentTransactionWindowEntity {
    @Id
    private String consentId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "consent_id")
    private ConsentEntity consent;

    @Column(name = "window_start", nullable = false)
    private OffsetDateTime from;

    @Column(name = "window_end", nullable = false)
    private OffsetDateTime to;

    static ConsentTransactionWindowEntity of(ConsentEntity consent, TransactionWindow window) {
        ConsentTransactionWindowEntity e = new ConsentTransactionWindowEntity();
        e.consent = consent;
        e.update(window);
        return e;
    }

    void update(TransactionWindow window) {
        this.from = window.from();
        this.to = window.to();
    }

    TransactionWindow toDomain() {
        return new TransactionWindow(from, to);
    }
}
