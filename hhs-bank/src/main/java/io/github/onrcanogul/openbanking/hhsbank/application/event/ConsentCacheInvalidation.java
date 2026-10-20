package io.github.onrcanogul.openbanking.hhsbank.application.event;

import java.util.Objects;

public record ConsentCacheInvalidation(String consentId) {

    public ConsentCacheInvalidation {
        Objects.requireNonNull(consentId, "consentId must not be null");
    }
}
