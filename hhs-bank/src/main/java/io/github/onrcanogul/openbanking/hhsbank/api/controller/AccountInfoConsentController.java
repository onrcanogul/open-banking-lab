package io.github.onrcanogul.openbanking.hhsbank.api.controller;

import io.github.onrcanogul.openbanking.hhsbank.api.model.AccountInfoConsentRequest;
import io.github.onrcanogul.openbanking.hhsbank.api.model.AccountInfoConsentResponse;
import io.github.onrcanogul.openbanking.hhsbank.application.consent.ConsentCommandService;
import io.github.onrcanogul.openbanking.hhsbank.application.consent.ConsentContextResolver;
import io.github.onrcanogul.openbanking.hhsbank.application.consent.ConsentQueryService;
import io.github.onrcanogul.openbanking.hhsbank.application.consent.mapper.AccountInfoConsentMapper;
import io.github.onrcanogul.openbanking.hhsbank.application.consent.model.CreateConsentCommand;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.Consent;
import io.github.onrcanogul.openbanking.hhsbank.security.AccessTokenConsentResolver;
import io.github.onrcanogul.openbanking.hhsbank.security.TppContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/ohvps/hbh/s2.0/hesap-bilgisi-rizasi")
@RequiredArgsConstructor
class AccountInfoConsentController {

    private final ConsentCommandService commandService;
    private final ConsentQueryService queryService;
    private final AccountInfoConsentMapper mapper;
    private final AccessTokenConsentResolver accessTokenConsentResolver;

    @PostMapping
    ResponseEntity<AccountInfoConsentResponse> create(
            @AuthenticationPrincipal TppContext tpp,
            @RequestHeader("X-ASPSP-Code") String aspspCodeHeader,
            @RequestHeader("X-TPP-Code") String tppCodeHeader,
            @Valid @RequestBody AccountInfoConsentRequest request) {

        CreateConsentCommand command = mapper.toCommand(request, tpp, aspspCodeHeader, tppCodeHeader);
        Consent created = commandService.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @GetMapping("/{rizaNo}")
    AccountInfoConsentResponse get(@AuthenticationPrincipal TppContext tpp,
                                   @PathVariable("rizaNo") String consentId) {
        return mapper.toResponse(queryService.getForTpp(consentId, tpp.tppCode()));
    }

    @DeleteMapping("/{rizaNo}")
    ResponseEntity<Void> revoke(@AuthenticationPrincipal TppContext tpp,
                                @RequestHeader(name = "X-Access-Token", required = false) String accessToken,
                                @PathVariable("rizaNo") String consentId) {

        Optional<String> tokenConsentId = accessTokenConsentResolver.consentIdOf(accessToken);
        commandService.revokeByTpp(consentId, tpp.tppCode(), tokenConsentId);
        return ResponseEntity.noContent().build();
    }
}
