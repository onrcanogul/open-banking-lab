package io.github.onrcanogul.openbanking.authsimulator.token;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
class JwksController {

    private final AccessTokenIssuer issuer;

    @GetMapping("/.well-known/jwks.json")
    Map<String, Object> jwks() {
        return issuer.publicJwks();
    }
}
