package io.github.onrcanogul.openbanking.hhsbank.common.exception;

import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.ConsentPermission;

public class AccountNotInConsentException extends RuntimeException {
    public AccountNotInConsentException(String consentId, String accountRef) {
        super(
                "Consent: " + consentId + ", Account: " + accountRef + " combination is not allowed!"
        );
    }
}
