package io.github.onrcanogul.openbanking.hhsbank.infrastructure.persistence.entity;

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

    @Enumerated(EnumType.STRING)                   // ORDINAL asla: enum sırası değişince veri bozulur
    @Column(name = "status", nullable = false, length = 30)
    private ConsentStatus status;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

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

    public Consent toDomain() {
        return new Consent(consentId, customerId, tppCode, status,
                Set.copyOf(permissions), Set.copyOf(accountRefs), expiresAt);
    }

    public static ConsentEntity fromDomain(Consent c) {
        ConsentEntity e = new ConsentEntity();
        e.consentId = c.consentId();
        e.customerId = c.customerId();
        e.tppCode = c.tppCode();
        return e.apply(c);
    }

    public ConsentEntity apply(Consent c) {
        this.status = c.status();
        this.expiresAt = c.expiresAt();
        this.permissions.clear();
        this.permissions.addAll(c.permissions());
        this.accountRefs.clear();
        this.accountRefs.addAll(c.accountRefs());
        return this;
    }
}
