package io.github.onrcanogul.openbanking.hhsbank.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;


import java.time.OffsetDateTime;
import java.util.List;

public record AccountInfoConsentRequest(
        @JsonProperty("oncekiRizaNo") String previousConsentId,
        @JsonProperty("katilimciBlg") @NotNull @Valid ParticipantInfo participant,
        @JsonProperty("gkd")          @NotNull @Valid GkdRequest gkd,
        @JsonProperty("kmlk")         @NotNull @Valid Identity identity,
        @JsonProperty("hspBlg")       @NotNull @Valid AccountInfoScope scope) {}

