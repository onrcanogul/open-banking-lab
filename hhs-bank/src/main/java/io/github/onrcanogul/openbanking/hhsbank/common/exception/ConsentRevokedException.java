package io.github.onrcanogul.openbanking.hhsbank.common.exception;

import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.ConsentStatus;

public class ConsentRevokedException extends RuntimeException {
    public ConsentRevokedException(String consentId, ConsentStatus status) {
    }
}
