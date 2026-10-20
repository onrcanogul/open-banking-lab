package io.github.onrcanogul.openbanking.hhsbank.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ohvps.bank")
public record BankProperties (String aspspCode) {
}
