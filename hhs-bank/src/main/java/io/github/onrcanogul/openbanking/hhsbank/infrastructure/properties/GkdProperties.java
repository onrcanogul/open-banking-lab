package io.github.onrcanogul.openbanking.hhsbank.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ohvps.gkd")
public record GkdProperties(
        String redirectBaseUrl
) {
}
