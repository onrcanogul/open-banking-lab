package io.github.onrcanogul.openbanking.authsimulator.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties("simulator")
public record SimulatorProperties(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9]{4}") String aspspCode,     // hhsKod this channel belongs to
        @NotBlank String issuer,                                            // iss of the erişim belirteci
        @NotBlank String audience,                                          // aud hhs-bank expects
        @NotNull Duration accessTokenTtl,
        @NotNull Duration authorizationCodeTtl,
        @NotBlank @Pattern(regexp = "https?://.+") String hhsBankBaseUrl,
        @NotBlank String gkdEventsTopic) {
}
