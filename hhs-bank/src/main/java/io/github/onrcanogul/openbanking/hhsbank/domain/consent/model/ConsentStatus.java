package io.github.onrcanogul.openbanking.hhsbank.domain.consent.model;

public enum ConsentStatus {
    AWAITING_AUTHORIZATION("B"),
    AUTHORIZED("Y"),
    AUTHORIZATION_USED("K"),
    TERMINATED("S"),
    REVOKED("I");

    private final String ohvpsCode;
    ConsentStatus(String ohvpsCode) { this.ohvpsCode = ohvpsCode; }
    public String ohvpsCode() { return ohvpsCode; }

    public boolean isFinal() { return this == TERMINATED || this == REVOKED; }
}
