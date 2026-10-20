package io.github.onrcanogul.openbanking.hhsbank.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GkdRequest(
        @JsonProperty("yetYntm")  @Pattern(regexp = "[YA]") String method,          // İ: Y yönlendirmeli, A ayrık
        @JsonProperty("yonAdr")   @Size(max = 1024) String redirectUrl,             // K: yönlendirmeli akışta zorunlu
        @JsonProperty("ayrikGkd") @Valid DecoupledGkd decoupled) {}                 // K: ayrık akışta zorunlu
