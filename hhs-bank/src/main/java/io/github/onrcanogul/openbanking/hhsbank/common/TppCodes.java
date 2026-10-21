package io.github.onrcanogul.openbanking.hhsbank.common;

import java.util.Locale;

public final class TppCodes {

    private TppCodes() {}

    public static String normalize(String tppCode) {
        return tppCode == null ? null : tppCode.toUpperCase(Locale.ROOT);
    }
}
