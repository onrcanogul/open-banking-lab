package io.github.onrcanogul.openbanking.hhsbank.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record Identity(
        @JsonProperty("kmlkTur")    @NotBlank @Size(max = 1) String type,
        @JsonProperty("kmlkVrs")    @NotBlank @Size(max = 30) String value,
        @JsonProperty("krmKmlkTur") @Size(max = 1) String corporateType,
        @JsonProperty("krmKmlkVrs") @Size(max = 30) String corporateValue,
        @JsonProperty("ohkTur")     @NotBlank @Pattern(regexp = "[BK]") String customerType) {}  // B bireysel, K kurumsal
