package io.github.onrcanogul.openbanking.hhsbank.infrastructure.persistence.entity;

import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.GkdAuthorization;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "consent_gkd")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsentGkdEntity {
    @Id
    private String consentId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "consent_id")
    private ConsentEntity consent;

    @Column(name = "method", nullable = false, length = 1)
    private String method;

    @Column(name = "redirect_url", length = 1024)
    private String redirectUrl;

    @Column(name = "deadline", nullable = false)
    private OffsetDateTime deadline;

    static ConsentGkdEntity of(ConsentEntity consent, GkdAuthorization gkd) {
        ConsentGkdEntity e = new ConsentGkdEntity();
        e.consent = consent;
        e.update(gkd);
        return e;
    }

    void update(GkdAuthorization gkd) {
        this.method = gkd.method();
        this.redirectUrl = gkd.redirectUrl();
        this.deadline = gkd.deadline();
    }

    GkdAuthorization toDomain() {
        return new GkdAuthorization(method, redirectUrl, deadline);
    }
}
