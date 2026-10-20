package io.github.onrcanogul.openbanking.hhsbank.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConsentInfo(
        @JsonProperty("rizaNo")        String consentId,
        @JsonProperty("olusZmn")       OffsetDateTime createdAt,
        @JsonProperty("gnclZmn")       OffsetDateTime updatedAt,
        @JsonProperty("rizaDrm")       String status,                   // B, Y, K, S, I
        @JsonProperty("rizaIptDtyKod") String cancellationReasonCode) {} // Sadece I'da
