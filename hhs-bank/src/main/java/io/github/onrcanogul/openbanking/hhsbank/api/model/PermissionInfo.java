package io.github.onrcanogul.openbanking.hhsbank.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.OffsetDateTime;
import java.util.List;

public record PermissionInfo(
        @JsonProperty("iznTur") @NotEmpty List<@Pattern(regexp = "0[1-9]") String> types,
        @JsonProperty("erisimIzniSonTrh") @NotNull OffsetDateTime accessExpiresAt,
        @JsonProperty("hesapIslemBslZmn") OffsetDateTime transactionFrom,          // K: 04 veya 05 seçildiyse zorunlu
        @JsonProperty("hesapIslemBtsZmn") OffsetDateTime transactionTo) {}
