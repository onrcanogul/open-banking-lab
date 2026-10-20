package io.github.onrcanogul.openbanking.hhsbank.domain.consent.model;

import java.util.Arrays;

public enum ConsentPermission {
    ACCOUNT_BASIC("01"), ACCOUNT_DETAIL("02"), BALANCE("03"),
    TRANSACTION_BASIC("04"), TRANSACTION_DETAIL("05"), INSTANT_BALANCE("06"),
    CARD_BASIC("07"), CARD_DETAIL("08"), CARD_TRANSACTION_DETAIL("09");

    private final String code;
    ConsentPermission(String code) { this.code = code; }
    public String code() { return code; }

    public static ConsentPermission fromCode(String code) {
        return Arrays.stream(values()).filter(p -> p.code.equals(code)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown permission code: " + code));
    }
}