package io.github.onrcanogul.openbanking.hhsbank.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AccountInfoConsentResponse(
        @JsonProperty("oncekiRizaNo") String previousConsentId,
        @JsonProperty("rzBlg")        ConsentInfo consent,
        @JsonProperty("kmlk")         Identity identity,
        @JsonProperty("katilimciBlg") ParticipantInfo participant,
        @JsonProperty("gkd")          GkdResponse gkd,
        @JsonProperty("hspBlg")       AccountInfoScope scope) {}

