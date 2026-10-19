package io.github.onrcanogul.openbanking.hhsbank.common.exception;

public class ConsentNotFoundException extends RuntimeException {
    public ConsentNotFoundException(String consentId) {
        super(
                consentId + " Consent not found!"
        );
    }
}
