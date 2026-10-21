package io.github.onrcanogul.openbanking.hhsbank.common.exception;

import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.ConsentStatus;

public class ConsentNotActiveException extends RuntimeException {

    private final ConsentStatus status;

    public ConsentNotActiveException(String consentId, ConsentStatus status) {
        super("Consent: " + consentId + " Status: " + status.name() + " combination is not active!");
        this.status = status;
    }

    public ConsentStatus status() {
        return status;
    }
}
