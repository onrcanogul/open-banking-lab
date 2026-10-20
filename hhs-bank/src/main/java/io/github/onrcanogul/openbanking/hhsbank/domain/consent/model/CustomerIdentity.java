package io.github.onrcanogul.openbanking.hhsbank.domain.consent.model;

public record CustomerIdentity(String type, String value,
                               String corporateType, String corporateValue,
                               String customerType) {      // B: bireysel, K: kurumsal
    public boolean isCorporate() { return "K".equals(customerType); }
}

