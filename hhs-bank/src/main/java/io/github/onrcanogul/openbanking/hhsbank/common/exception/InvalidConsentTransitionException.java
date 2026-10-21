package io.github.onrcanogul.openbanking.hhsbank.common.exception;

import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.ConsentStatus;

public class InvalidConsentTransitionException extends RuntimeException {
    public InvalidConsentTransitionException(String consentId, ConsentStatus status, ConsentStatus expected) {
        super("Consent: " + consentId + " in status " + status.name() + " cannot make this transition"
                + (expected == null ? "" : " (expected status " + expected.name() + ")"));
    }
}
