package io.github.onrcanogul.openbanking.hhsbank.infrastructure.persistence.entity;

import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.CancellationReason;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.Consent;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.ConsentPermission;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.ConsentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "consent")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsentEntity {
    @Id
    @Column(name = "consent_id", length = 64)
    private String consentId;

    @Column(name = "customer_id", nullable = false, length = 40)
    private String customerId;

    @Column(name = "tpp_code", nullable = false, length = 10)
    private String tppCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ConsentStatus status;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_status", length = 30)
    private CancellationReason cancellationReason;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @ElementCollection
    @CollectionTable(name = "consent_permission", joinColumns = @JoinColumn(name = "consent_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permission", length = 30)
    private Set<ConsentPermission> permissions = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "consent_account", joinColumns = @JoinColumn(name = "consent_id"))
    @Column(name = "account_ref", length = 40)
    private Set<String> accountRefs = new HashSet<>();

    @OneToOne(mappedBy = "consent", cascade = CascadeType.ALL, orphanRemoval = true)
    private ConsentIdentityEntity identity;

    @OneToOne(mappedBy = "consent", cascade = CascadeType.ALL, orphanRemoval = true)
    private ConsentGkdEntity gkd;

    @OneToOne(mappedBy = "consent", cascade = CascadeType.ALL, orphanRemoval = true)
    private ConsentTransactionWindowEntity transactionWindow;

    public Consent toDomain() {
        return new Consent(consentId, customerId, tppCode, status, cancellationReason,
                Set.copyOf(permissions), Set.copyOf(accountRefs), expiresAt, createdAt, updatedAt,
                identity == null ? null : identity.toDomain(),
                transactionWindow == null ? null : transactionWindow.toDomain(),
                gkd == null ? null : gkd.toDomain());
    }

    public static ConsentEntity fromDomain(Consent c) {
        ConsentEntity e = new ConsentEntity();
        e.consentId = c.consentId();
        e.customerId = c.customerId();
        e.tppCode = c.tppCode();
        e.createdAt = c.createdAt();
        return e.apply(c);
    }

    public ConsentEntity apply(Consent c) {
        this.status = c.status();
        this.cancellationReason = c.cancellationReason();
        this.expiresAt = c.expiresAt();
        this.updatedAt = c.updatedAt();
        this.permissions.clear();
        this.permissions.addAll(c.permissions());
        this.accountRefs.clear();
        this.accountRefs.addAll(c.accountRefs());

        if (c.identity() == null) {
            this.identity = null;
        } else if (this.identity == null) {
            this.identity = ConsentIdentityEntity.of(this, c.identity());
        } else {
            this.identity.update(c.identity());
        }

        if (c.gkd() == null) {
            this.gkd = null;
        } else if (this.gkd == null) {
            this.gkd = ConsentGkdEntity.of(this, c.gkd());
        } else {
            this.gkd.update(c.gkd());
        }

        if (c.transactionWindow() == null) {
            this.transactionWindow = null;
        } else if (this.transactionWindow == null) {
            this.transactionWindow = ConsentTransactionWindowEntity.of(this, c.transactionWindow());
        } else {
            this.transactionWindow.update(c.transactionWindow());
        }
        return this;
    }
}
