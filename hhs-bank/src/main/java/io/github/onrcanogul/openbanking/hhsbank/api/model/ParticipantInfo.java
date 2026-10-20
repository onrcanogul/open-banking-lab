package io.github.onrcanogul.openbanking.hhsbank.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ParticipantInfo(
        @JsonProperty("hhsKod") @NotBlank @Size(min = 4, max = 4) String aspspCode,
        @JsonProperty("yosKod") @NotBlank @Size(min = 4, max = 4) String tppCode) {}
