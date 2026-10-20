package io.github.onrcanogul.openbanking.hhsbank.application.consent.model;

import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.ConsentPermission;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.CustomerIdentity;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.TransactionWindow;

import java.time.OffsetDateTime;
import java.util.Set;

public record CreateConsentCommand(
        String authenticatedTppCode,
        String aspspCodeHeader,        // X-ASPSP-Code
        String tppCodeHeader,          // X-TPP-Code
        String aspspCodeBody,          // katilimciBlg.hhsKod
        String tppCodeBody,            // katilimciBlg.yosKod
        String previousConsentId,
        CustomerIdentity identity,
        Set<ConsentPermission> permissions,
        OffsetDateTime expiresAt,
        TransactionWindow transactionWindow,
        String gkdMethod,
        String redirectUrl,
        boolean decoupledRequested) {}
