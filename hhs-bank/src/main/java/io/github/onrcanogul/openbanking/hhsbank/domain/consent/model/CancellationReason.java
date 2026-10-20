package io.github.onrcanogul.openbanking.hhsbank.domain.consent.model;

import java.util.Arrays;

public enum CancellationReason {
    NEW_CONSENT_REQUEST("01"),
    CUSTOMER_VIA_HHS("02"),
    CUSTOMER_VIA_TPP("03"),
    TIMEOUT_AWAITING_AUTHORIZATION("04"),
    TIMEOUT_AUTHORIZED("05"),
    GKD_DUPLICATE_CALL("07"),
    GKD_IDENTITY_MISMATCH("08"),
    GKD_NO_ELIGIBLE_PRODUCT("09"),
    GKD_CHANNEL_CLOSED("10"),
    GKD_ACCOUNT_AUTHORITY("11"),
    GKD_HHS_CHECKS_FAILED("12"),
    GKD_CUSTOMER_ABORTED("13"),
    GKD_FRAUD_SUSPICION("14"),
    CONSENT_UPDATE("15"),
    GKD_OTHER("99");

    private final String code;
    CancellationReason(String code) { this.code = code; }
    public String code() { return code; }

    public static CancellationReason fromCode(String code) {
        return Arrays.stream(values()).filter(r -> r.code.equals(code)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown cancellation code: " + code));
    }

    public boolean isGkdFailure() {
        return switch (this) {
            case GKD_DUPLICATE_CALL, GKD_IDENTITY_MISMATCH, GKD_NO_ELIGIBLE_PRODUCT,
                 GKD_CHANNEL_CLOSED, GKD_ACCOUNT_AUTHORITY, GKD_HHS_CHECKS_FAILED,
                 GKD_CUSTOMER_ABORTED, GKD_FRAUD_SUSPICION, GKD_OTHER -> true;
            default -> false;
        };
    }
}
