package io.github.onrcanogul.openbanking.hhsbank.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record AccountInfoScope(
        @JsonProperty("iznBlg") @NotNull @Valid PermissionInfo permission) {}
