package io.github.onrcanogul.openbanking.hhsbank.security;

import io.github.onrcanogul.openbanking.hhsbank.common.TppCodes;

import java.util.Objects;

public record TppContext(String tppCode) {
    public TppContext {
        Objects.requireNonNull(tppCode, "tppCode");
        tppCode = TppCodes.normalize(tppCode);
    }
}
