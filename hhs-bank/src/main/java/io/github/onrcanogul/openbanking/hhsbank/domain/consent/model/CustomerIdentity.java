package io.github.onrcanogul.openbanking.hhsbank.domain.consent.model;

public record CustomerIdentity(String type, String value,
                               String corporateType, String corporateValue,
                               String customerType) {      // B: bireysel, K: kurumsal
    public boolean isCorporate() { return "K".equals(customerType); }

    @Override
    public String toString() {
        return "CustomerIdentity[type=" + type + ", value=" + mask(value)
                + ", corporateType=" + corporateType + ", corporateValue=" + mask(corporateValue)
                + ", customerType=" + customerType + "]";
    }

    private static String mask(String raw) {
        if (raw == null) return null;
        return raw.length() <= 4 ? "****" : "*".repeat(raw.length() - 4) + raw.substring(raw.length() - 4);
    }
}

