package io.github.onrcanogul.openbanking.hhsbank.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("ohvps.security")
public record OhvpsSecurityProperties(
        String issuer,
        String jwkSetUri,
        String audience,
        Duration clockSkew) {}
