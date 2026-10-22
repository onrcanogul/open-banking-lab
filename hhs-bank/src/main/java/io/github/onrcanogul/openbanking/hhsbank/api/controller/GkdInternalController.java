package io.github.onrcanogul.openbanking.hhsbank.api.controller;

import io.github.onrcanogul.openbanking.hhsbank.api.model.GkdConsentResponse;
import io.github.onrcanogul.openbanking.hhsbank.application.consent.GkdConsentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/gkd/riza")
@RequiredArgsConstructor
class GkdInternalController {

    private final GkdConsentQueryService service;

    @GetMapping("/{rizaNo}")
    GkdConsentResponse get(@PathVariable("rizaNo") String consentId) {
        return GkdConsentResponse.from(service.get(consentId));
    }
}
