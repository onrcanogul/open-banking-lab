package io.github.onrcanogul.openbanking.hhsbank.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Two token issuers: Keycloak signs client tokens (TPP client_credentials and internal service tokens),
 * the bank's own GKD service (auth-simulator) signs the customer access token (erişim belirteci) that carries riza_no.
 */
@Validated
@ConfigurationProperties("ohvps.security")
public record OhvpsSecurityProperties(
        @NotBlank String audience,
        @NotNull Duration clockSkew,
        @NotNull @Valid Issuer clientIssuer,
        @NotNull @Valid Issuer customerIssuer) {

    public record Issuer(@NotBlank String issuer, @NotBlank String jwkSetUri) {}
}
