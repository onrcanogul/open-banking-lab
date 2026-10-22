package io.github.onrcanogul.openbanking.authsimulator.consent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HhsBankConsentClient implements ConsentLookup {

    private final RestClient hhsBankRestClient;

    @Override
    public Optional<GkdConsent> find(String consentId) {
        try {
            return Optional.ofNullable(hhsBankRestClient.get()
                    .uri("/internal/gkd/riza/{rizaNo}", consentId)
                    .retrieve()
                    .body(GkdConsent.class));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }
}
