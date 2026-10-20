package io.github.onrcanogul.openbanking.hhsbank.domain.consent.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum ConsentStatus {
    AWAITING_AUTHORIZATION("B"),
    AUTHORIZED("Y"),
    AUTHORIZATION_USED("K"),
    TERMINATED("S"),
    REVOKED("I");

    private static final Set<ConsentStatus> OPEN =
            Collections.unmodifiableSet(EnumSet.of(AWAITING_AUTHORIZATION, AUTHORIZED, AUTHORIZATION_USED));

    public static Set<ConsentStatus> openStatuses() { return OPEN; }

    public boolean isOpen() { return OPEN.contains(this); }

    private final String ohvpsCode;
    ConsentStatus(String ohvpsCode) { this.ohvpsCode = ohvpsCode; }
    public String ohvpsCode() { return ohvpsCode; }

    public boolean isFinal() { return this == TERMINATED || this == REVOKED; }
}
