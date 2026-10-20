package io.github.onrcanogul.openbanking.hhsbank.security;

import java.util.Locale;
import java.util.Objects;

public record TppContext(String tppCode) {
    public TppContext {
        Objects.requireNonNull(tppCode, "tppCode");
        tppCode = tppCode.toUpperCase(Locale.ROOT);
    }
}