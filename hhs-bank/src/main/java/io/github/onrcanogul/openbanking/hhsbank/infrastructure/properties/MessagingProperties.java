package io.github.onrcanogul.openbanking.hhsbank.infrastructure.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

// Kafka topic names are shared contracts with other modules; blank values fail at startup instead of
// silently publishing to (or listening on) an auto-created topic.
@Validated
@ConfigurationProperties("ohvps.messaging")
public record MessagingProperties(@NotNull @Valid Topics topics) {

    public record Topics(
            @NotBlank String gkdEvents,
            @NotBlank String consentStatusChanged) {}
}
