package io.github.onrcanogul.openbanking.hhsbank.common.exception;

import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.ConsentStatus;

public class ConsentNotActiveException extends RuntimeException {
    public ConsentNotActiveException(String consentId, ConsentStatus status) {
        super(
                " Consent: "+ consentId + " Status: " + status.name() + " combination is not active!"
        );
    }
}
