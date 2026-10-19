package io.github.onrcanogul.openbanking.hhsbank.application.audit;

import io.github.onrcanogul.openbanking.hhsbank.application.consent.ConsentContext;

import java.util.List;

public interface DataAccessAuditor {
    void record(ConsentContext ctx, String type, List<String> accountRefs);
}
