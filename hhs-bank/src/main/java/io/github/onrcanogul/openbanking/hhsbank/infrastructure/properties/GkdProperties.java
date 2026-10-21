package io.github.onrcanogul.openbanking.hhsbank.infrastructure.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("ohvps.gkd")
public record GkdProperties(
        @NotBlank @Pattern(regexp = "https?://.+", message = "must be an absolute http(s) URL") String redirectBaseUrl
) {
}
