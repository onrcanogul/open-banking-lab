package io.github.onrcanogul.openbanking.hhsbank.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GkdResponse(
        @JsonProperty("yetYntm")   String method,
        @JsonProperty("yonAdr")    String redirectUrl,
        @JsonProperty("ayrikGkd")  DecoupledGkd decoupled,
        @JsonProperty("hhsYonAdr") String bankRedirectUrl,         // Müşterinin GKD için yönlendirileceği banka adresi
        @JsonProperty("yetTmmZmn") OffsetDateTime authorizationDeadline) {}   // Oluşturma + en fazla 5 dk
