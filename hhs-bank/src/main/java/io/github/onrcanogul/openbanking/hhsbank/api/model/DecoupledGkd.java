package io.github.onrcanogul.openbanking.hhsbank.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

public record DecoupledGkd(
        @JsonProperty("ohkTanimTip")   @Size(max = 8) String type,
        @JsonProperty("ohkTanimDeger") @Size(max = 30) String value) {}
