package io.github.onrcanogul.openbanking.authsimulator.token;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** ÖHVPS erişim belirteci isteği; with yetTip=yenileme_belirteci, yetKod carries the refresh token. */
public record TokenRequest(
        @JsonProperty("hhsKod")  @NotBlank String aspspCode,
        @JsonProperty("yosKod")  @NotBlank String tppCode,
        @JsonProperty("yetTip")  @NotBlank @Pattern(regexp = "yet_kod|yenileme_belirteci") String grantType,
        @JsonProperty("yetKod")  @NotBlank String code,
        @JsonProperty("rizaNo")  @NotBlank String consentId,
        @JsonProperty("rizaTip") @NotBlank @Pattern(regexp = "[HO]") String consentType) {

    public static final String AUTHORIZATION_CODE = "yet_kod";
    public static final String REFRESH_TOKEN = "yenileme_belirteci";
    public static final String ACCOUNT_INFO_CONSENT = "H";
}
