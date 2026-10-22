package io.github.onrcanogul.openbanking.authsimulator.consent;

import java.util.Optional;

public interface ConsentLookup {
    Optional<GkdConsent> find(String consentId);
}
