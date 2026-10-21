package io.github.onrcanogul.openbanking.hhsbank.application.consent.mapper;

import io.github.onrcanogul.openbanking.hhsbank.api.model.*;
import io.github.onrcanogul.openbanking.hhsbank.application.consent.model.CreateConsentCommand;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.*;
import io.github.onrcanogul.openbanking.hhsbank.infrastructure.properties.BankProperties;
import io.github.onrcanogul.openbanking.hhsbank.infrastructure.properties.GkdProperties;
import io.github.onrcanogul.openbanking.hhsbank.security.TppContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AccountInfoConsentMapper {

    private final GkdProperties gkdProps;
    private final BankProperties bank;

    public CreateConsentCommand toCommand(AccountInfoConsentRequest r, TppContext tpp,
                                          String aspspHeader, String tppHeader) {
        PermissionInfo p = r.scope().permission();
        TransactionWindow window = (p.transactionFrom() == null && p.transactionTo() == null)
                ? null : new TransactionWindow(p.transactionFrom(), p.transactionTo());

        return new CreateConsentCommand(
                tpp.tppCode(), aspspHeader, tppHeader,
                r.participant().aspspCode(), r.participant().tppCode(),
                r.previousConsentId(),
                new CustomerIdentity(r.identity().type(), r.identity().value(),
                        r.identity().corporateType(), r.identity().corporateValue(), r.identity().customerType()),
                p.types().stream().map(ConsentPermission::fromCode).collect(Collectors.toSet()),
                p.accessExpiresAt(), window,
                r.gkd().method(), r.gkd().redirectUrl(),
                "A".equals(r.gkd().method()));
    }

    public AccountInfoConsentResponse toResponse(Consent c) {
        CustomerIdentity id = c.identity();
        GkdAuthorization g = c.gkd();
        TransactionWindow w = c.transactionWindow();
        return new AccountInfoConsentResponse(
                null,
                new ConsentInfo(c.consentId(), c.createdAt(), c.updatedAt(), c.status().ohvpsCode(),
                        c.cancellationReason() == null ? null : c.cancellationReason().code()),
                id == null ? null : new Identity(id.type(), id.value(), id.corporateType(), id.corporateValue(), id.customerType()),
                new ParticipantInfo(bank.aspspCode(), c.tppCode()),
                g == null ? null : new GkdResponse(g.method(), g.redirectUrl(), null,
                        bankRedirectUrl(c), g.deadline()),
                new AccountInfoScope(new PermissionInfo(
                        c.permissions().stream().map(ConsentPermission::code).sorted().toList(),
                        c.expiresAt(),
                        w == null ? null : w.from(),
                        w == null ? null : w.to())));
    }

    private String bankRedirectUrl(Consent c) {
        if (c.status() != ConsentStatus.AWAITING_AUTHORIZATION) return null;
        return UriComponentsBuilder.fromUriString(gkdProps.redirectBaseUrl())
                .queryParam("rizaNo", c.consentId()).build().toUriString();
    }
}