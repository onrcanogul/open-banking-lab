package io.github.onrcanogul.openbanking.hhsbank.api.controller;

import io.github.onrcanogul.openbanking.hhsbank.application.consent.ConsentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ohvps/hbh/s2.0/hesap-bilgisi-rizasi")
@RequiredArgsConstructor
public class AccountInfoConsentController {
    private final ConsentQueryService consentQueryService;
}
