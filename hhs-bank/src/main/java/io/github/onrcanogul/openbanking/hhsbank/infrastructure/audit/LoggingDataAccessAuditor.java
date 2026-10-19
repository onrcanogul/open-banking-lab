package io.github.onrcanogul.openbanking.hhsbank.infrastructure.audit;

import io.github.onrcanogul.openbanking.hhsbank.application.audit.DataAccessAuditor;
import io.github.onrcanogul.openbanking.hhsbank.application.consent.ConsentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class LoggingDataAccessAuditor implements DataAccessAuditor {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    @Override
    public void record(ConsentContext ctx, String type, List<String> accountRefs) {
        AUDIT.info("type={} consentId={} tpp={} customer={} accounts={}",
                type, ctx.consentId(), ctx.tppCode(), ctx.customerId(), accountRefs);
    }
}
