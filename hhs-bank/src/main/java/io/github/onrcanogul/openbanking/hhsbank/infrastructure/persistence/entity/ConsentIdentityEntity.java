package io.github.onrcanogul.openbanking.hhsbank.infrastructure.persistence.entity;

import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.CustomerIdentity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "consent_identity")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsentIdentityEntity {
    @Id
    private String consentId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "consent_id")
    private ConsentEntity consent;

    @Column(name = "identity_type", nullable = false, length = 1)
    private String type;

    @Column(name = "identity_value", nullable = false, length = 30)
    private String value;

    @Column(name = "corporate_type", length = 1)
    private String corporateType;

    @Column(name = "corporate_value", length = 30)
    private String corporateValue;

    @Column(name = "customer_type", nullable = false, length = 1)
    private String customerType;

    static ConsentIdentityEntity of(ConsentEntity consent, CustomerIdentity identity) {
        ConsentIdentityEntity e = new ConsentIdentityEntity();
        e.consent = consent;
        e.update(identity);
        return e;
    }

    void update(CustomerIdentity identity) {
        this.type = identity.type();
        this.value = identity.value();
        this.corporateType = identity.corporateType();
        this.corporateValue = identity.corporateValue();
        this.customerType = identity.customerType();
    }

    CustomerIdentity toDomain() {
        return new CustomerIdentity(type, value, corporateType, corporateValue, customerType);
    }
}
