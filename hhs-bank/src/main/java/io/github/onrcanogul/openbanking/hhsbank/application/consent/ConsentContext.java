package io.github.onrcanogul.openbanking.hhsbank.application.consent;

import io.github.onrcanogul.openbanking.hhsbank.common.exception.AccountNotInConsentException;
import io.github.onrcanogul.openbanking.hhsbank.common.exception.ConsentPermissionException;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.ConsentPermission;

import java.util.Set;

public record ConsentContext (
        String consentId,
        String customerId,
        String tppCode,
        Set<ConsentPermission> permissions,
        Set<String> accountRefs
) {
    public void requirePermission(ConsentPermission permission) {
        if (!permissions.contains(permission)) {
            throw new ConsentPermissionException(consentId, permission);
        }
    }

    public void requireAccount(String accountRef) {
        if (!accountRefs.contains(accountRef)) {
            throw new AccountNotInConsentException(consentId, accountRef);
        }
    }
}
