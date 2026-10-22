package io.github.onrcanogul.openbanking.hhsbank.api.model;

import io.github.onrcanogul.openbanking.hhsbank.application.consent.GkdConsentQueryService.GkdConsentView;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.Consent;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.ConsentPermission;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.CustomerIdentity;

import java.time.OffsetDateTime;
import java.util.List;

/** Bank-internal contract with the GKD channel (auth-simulator); not an ÖHVPS resource. */
public record GkdConsentResponse(
        String consentId,
        String status,                       // ÖHVPS rizaDrm code: B, Y, K, S, I
        String tppCode,
        String customerId,
        IdentityView identity,
        List<String> permissions,            // ÖHVPS iznTur codes
        OffsetDateTime expiresAt,
        OffsetDateTime authorizationDeadline,
        String redirectUrl,                  // TPP yonAdr
        List<AccountView> accounts) {

    public record IdentityView(String type, String value, String customerType) {}

    public record AccountView(String accountRef, String iban, String currency) {}

    public static GkdConsentResponse from(GkdConsentView view) {
        Consent c = view.consent();
        CustomerIdentity id = c.identity();
        return new GkdConsentResponse(
                c.consentId(), c.status().ohvpsCode(), c.tppCode(), c.customerId(),
                id == null ? null : new IdentityView(id.type(), id.value(), id.customerType()),
                c.permissions().stream().map(ConsentPermission::code).sorted().toList(),
                c.expiresAt(),
                c.gkd() == null ? null : c.gkd().deadline(),
                c.gkd() == null ? null : c.gkd().redirectUrl(),
                view.customerAccounts().stream()
                        .map(a -> new AccountView(a.accountRef(), a.iban(), a.currency().name()))
                        .toList());
    }
}
