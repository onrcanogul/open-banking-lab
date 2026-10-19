package io.github.onrcanogul.openbanking.hhsbank.common.exception;

import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.ConsentPermission;

public class ConsentPermissionException extends RuntimeException {
    public ConsentPermissionException(String consentId, ConsentPermission permission) {
        super(
               "Consent: " + consentId + ", Permission: " + permission + " combination is not allowed!"
        );
    }
}
