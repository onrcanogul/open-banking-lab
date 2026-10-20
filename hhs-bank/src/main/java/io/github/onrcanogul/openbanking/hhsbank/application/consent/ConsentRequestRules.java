package io.github.onrcanogul.openbanking.hhsbank.application.consent;

import io.github.onrcanogul.openbanking.hhsbank.application.consent.model.CreateConsentCommand;
import io.github.onrcanogul.openbanking.hhsbank.common.exception.OhvpsErrorCode;
import io.github.onrcanogul.openbanking.hhsbank.common.exception.OhvpsException;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.ConsentPermission;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.CustomerIdentity;
import io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.TransactionWindow;
import io.github.onrcanogul.openbanking.hhsbank.infrastructure.properties.BankProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Set;

import static io.github.onrcanogul.openbanking.hhsbank.domain.consent.model.ConsentPermission.*;

@Component
@RequiredArgsConstructor
public class ConsentRequestRules {

    private final BankProperties bankProperties;
    private final Clock clock;

    public void validate(CreateConsentCommand command) {
        validateParticipants(command);
        validateUnsupportedFlows(command);
        validatePermissions(command.permissions());
        validateAccessExpiry(command.expiresAt(), command.identity());
        validateTransactionWindow(command.permissions(), command.transactionWindow());
        validateRedirect(command);
    }

    private void validateParticipants(CreateConsentCommand cmd) {
        if (!bankProperties.aspspCode().equals(cmd.aspspCodeBody()) || !cmd.aspspCodeBody().equals(cmd.aspspCodeHeader())) {
            throw new OhvpsException(OhvpsErrorCode.INVALID_ASPSP, "hhsKod mismatch");
        }
        if (!cmd.tppCodeBody().equals(cmd.tppCodeHeader())
                || !cmd.tppCodeHeader().equalsIgnoreCase(cmd.authenticatedTppCode())) {
            throw new OhvpsException(OhvpsErrorCode.INVALID_TPP, "yosKod mismatch");
        }
    }

    private void validateUnsupportedFlows(CreateConsentCommand cmd) {
        if (cmd.decoupledRequested()) {
            throw new OhvpsException(OhvpsErrorCode.DECOUPLED_AUTHENTICATION_NOT_SUPPORTED, "Decoupled GKD not supported");
        }
        if (cmd.previousConsentId() != null) {
            throw new OhvpsException(OhvpsErrorCode.CONSENT_STATUS_NOT_FOR_UPDATE, "Consent update flow not supported");
        }
    }

    private void validatePermissions(Set<ConsentPermission> p) {
        boolean accountBasic = p.contains(ACCOUNT_BASIC);
        boolean cardBasic = p.contains(CARD_BASIC);

        if (!accountBasic && !cardBasic) {
            throw incorrectPermission("01 or 07 required");
        }
        if (!accountBasic && containsAny(p, ACCOUNT_DETAIL, BALANCE, TRANSACTION_BASIC,
                TRANSACTION_DETAIL, INSTANT_BALANCE)) {
            throw incorrectPermission("02-06 require 01");
        }
        if (!cardBasic && containsAny(p, CARD_DETAIL, CARD_TRANSACTION_DETAIL)) {
            throw incorrectPermission("08-09 require 07");
        }
        if (p.contains(INSTANT_BALANCE) && !p.contains(BALANCE)) {
            throw incorrectPermission("06 requires 03");
        }
    }

    private void validateAccessExpiry(OffsetDateTime expiresAt, CustomerIdentity identity) {
        LocalDate today = LocalDate.now(clock);
        ZoneOffset offset = OffsetDateTime.now(clock).getOffset();
        OffsetDateTime min = today.plusDays(2).atStartOfDay().atOffset(offset);
        OffsetDateTime max = today.plusMonths(identity.isCorporate() ? 12 : 6).plusDays(1).atStartOfDay().atOffset(offset);

        if (expiresAt.isBefore(min) || expiresAt.isAfter(max)) {
            throw new OhvpsException(OhvpsErrorCode.INVALID_FORMAT, "erisimIzniSonTrh out of range");
        }
    }

    private void validateTransactionWindow(Set<ConsentPermission> p, TransactionWindow w) {
        boolean needsWindow = p.contains(TRANSACTION_BASIC) || p.contains(TRANSACTION_DETAIL);
        if (!needsWindow) {
            if (w != null) throw new OhvpsException(OhvpsErrorCode.INVALID_FORMAT, "Transaction window not allowed");
            return;
        }
        if (w == null || w.from() == null || w.to() == null) {
            throw new OhvpsException(OhvpsErrorCode.INVALID_FORMAT, "Transaction window required");
        }
        OffsetDateTime now = OffsetDateTime.now(clock);
        if (w.from().isBefore(now.minusMonths(12)) || w.to().isAfter(now.plusMonths(12)) || w.from().isAfter(w.to())) {
            throw new OhvpsException(OhvpsErrorCode.INVALID_FORMAT, "Transaction window out of range");
        }
    }

    private void validateRedirect(CreateConsentCommand cmd) {
        if (cmd.redirectUrl() == null || cmd.redirectUrl().isBlank()) {
            throw new OhvpsException(OhvpsErrorCode.INVALID_FORMAT, "yonAdr required for redirect flow");
        }
        // TODO: Spec, yonAdr'nin YÖS API'deki kayıtlı adreslerle uyumunu da istiyor (TPPRedirectionAddressMismatch)
    }

    private static boolean containsAny(Set<ConsentPermission> set, ConsentPermission... values) {
        return Arrays.stream(values).anyMatch(set::contains);
    }

    private static OhvpsException incorrectPermission(String detail) {
        return new OhvpsException(OhvpsErrorCode.INCORRECT_PERMISSION_TYPE, detail);
    }
}
