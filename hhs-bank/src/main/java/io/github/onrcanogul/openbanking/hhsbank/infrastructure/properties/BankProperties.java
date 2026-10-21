package io.github.onrcanogul.openbanking.hhsbank.infrastructure.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("ohvps.bank")
public record BankProperties(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9]{4}", message = "must be 4 alphanumeric characters (hhsKod, AN4)") String aspspCode) {
}
