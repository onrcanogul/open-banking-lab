package io.github.onrcanogul.openbanking.authsimulator.token;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenResponse(
        @JsonProperty("erisimBelirteci")                   String accessToken,
        @JsonProperty("gecerlilikSuresi")                  long expiresInSeconds,
        @JsonProperty("yenilemeBelirteci")                 String refreshToken,
        @JsonProperty("yenilemeBelirteciGecerlilikSuresi") long refreshExpiresInSeconds) {}
