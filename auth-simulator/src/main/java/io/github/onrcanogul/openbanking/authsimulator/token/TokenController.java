package io.github.onrcanogul.openbanking.authsimulator.token;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ohvps/gkd/s2.0/erisim-belirteci")
@RequiredArgsConstructor
class TokenController {

    private final TokenService service;

    @PostMapping
    TokenResponse token(@AuthenticationPrincipal Jwt tppToken,
                        @RequestHeader(name = "X-Request-ID", required = false) String requestId,
                        @Valid @RequestBody TokenRequest request) {
        if (requestId != null) {
            MDC.put("requestId", requestId);
        }
        try {
            return service.exchange(request, tppToken.getClaimAsString("azp"));
        } finally {
            MDC.remove("requestId");
        }
    }
}
