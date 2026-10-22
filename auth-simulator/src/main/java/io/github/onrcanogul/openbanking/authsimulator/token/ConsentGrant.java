package io.github.onrcanogul.openbanking.authsimulator.token;

/** What a yetKod or yenileme belirteci stands for. */
public record ConsentGrant(String consentId, String tppCode, String customerId) {

    boolean matches(String consentId, String tppCode) {
        return this.consentId.equals(consentId) && this.tppCode.equalsIgnoreCase(tppCode);
    }
}
