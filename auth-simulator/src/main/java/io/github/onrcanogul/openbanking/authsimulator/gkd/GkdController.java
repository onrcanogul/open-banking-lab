package io.github.onrcanogul.openbanking.authsimulator.gkd;

import io.github.onrcanogul.openbanking.authsimulator.consent.ConsentLookup;
import io.github.onrcanogul.openbanking.authsimulator.consent.GkdConsent;
import io.github.onrcanogul.openbanking.authsimulator.messaging.GkdEvent.GkdApproved;
import io.github.onrcanogul.openbanking.authsimulator.messaging.GkdEvent.GkdRejected;
import io.github.onrcanogul.openbanking.authsimulator.messaging.GkdEventPublisher;
import io.github.onrcanogul.openbanking.authsimulator.messaging.GkdEventPublisher.GkdEventPublishException;
import io.github.onrcanogul.openbanking.authsimulator.token.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * The page behind hhsYonAdr. The customer has just logged in at Keycloak (that login is the GKD);
 * here they see what the TPP asked for, pick the accounts and approve or reject. Either way the result goes
 * to hhs-bank as a GKD event and the browser goes back to the TPP's yonAdr.
 */
@Controller
@RequestMapping("/gkd")
@RequiredArgsConstructor
@Slf4j
class GkdController {

    // ÖHVPS rizaIptDtyKod values the GKD channel produces
    private static final String IDENTITY_MISMATCH = "08";
    private static final String CUSTOMER_ABORTED = "13";

    private static final Map<String, String> PERMISSION_LABELS = Map.of(
            "01", "Temel hesap bilgisi",
            "02", "Ayrıntılı hesap bilgisi",
            "03", "Bakiye bilgisi",
            "04", "Temel işlem (hesap hareketleri) bilgisi",
            "05", "Ayrıntılı işlem bilgisi",
            "06", "Anlık bakiye bildirimi",
            "07", "Temel kart bilgisi",
            "08", "Ayrıntılı kart bilgisi",
            "09", "Kart hareketleri bilgisi");

    private final ConsentLookup consents;
    private final GkdEventPublisher events;
    private final TokenService tokens;
    private final Clock clock;

    @GetMapping
    String show(@RequestParam("rizaNo") String consentId, @AuthenticationPrincipal OidcUser customer,
                Model model, HttpServletRequest request) {
        GkdConsent consent = pending(consentId);
        if (!consent.belongsTo(customer.getPreferredUsername())) {
            return rejectAndReturnToTpp(consent, IDENTITY_MISMATCH, request);
        }
        populate(model, consent, customer);
        return "gkd/approve";
    }

    @PostMapping("/approve")
    String approve(@RequestParam("rizaNo") String consentId,
                   @RequestParam(name = "hesaplar", required = false) List<String> selected,
                   @AuthenticationPrincipal OidcUser customer, Model model, HttpServletRequest request) {
        GkdConsent consent = pending(consentId);
        if (!consent.belongsTo(customer.getPreferredUsername())) {
            return rejectAndReturnToTpp(consent, IDENTITY_MISMATCH, request);
        }

        Set<String> accountRefs = selected == null ? Set.of() : Set.copyOf(selected);
        if (accountRefs.isEmpty() || !consent.accountRefs().containsAll(accountRefs)) {
            populate(model, consent, customer);
            model.addAttribute("error", "Devam etmek için en az bir hesap seçin.");
            return "gkd/approve";
        }

        events.publish(new GkdApproved(UUID.randomUUID().toString(), consent.consentId(),
                OffsetDateTime.now(clock), accountRefs));
        String authorizationCode = tokens.issueAuthorizationCode(consent);
        endGkdSession(request);
        return "redirect:" + tppRedirect(consent, "Y", authorizationCode);
    }

    @PostMapping("/reject")
    String reject(@RequestParam("rizaNo") String consentId, @AuthenticationPrincipal OidcUser customer,
                  HttpServletRequest request) {
        GkdConsent consent = pending(consentId);
        String reason = consent.belongsTo(customer.getPreferredUsername()) ? CUSTOMER_ABORTED : IDENTITY_MISMATCH;
        return rejectAndReturnToTpp(consent, reason, request);
    }

    @ExceptionHandler(GkdPageException.class)
    String gkdProblem(GkdPageException ex, Model model) {
        return message(model, ex.title(), ex.getMessage());
    }

    @ExceptionHandler({GkdEventPublishException.class, RestClientException.class})
    String downstreamFailure(RuntimeException ex, Model model) {
        log.warn("GKD step failed on a downstream dependency", ex);
        return message(model, "İşleminiz şu anda tamamlanamıyor",
                "Banka servislerine ulaşılamadı. Lütfen daha sonra yeniden deneyin.");
    }

    private GkdConsent pending(String consentId) {
        GkdConsent consent = consents.find(consentId)
                .orElseThrow(() -> new GkdPageException("Rıza bulunamadı", "Bu bağlantıya ait bir rıza kaydı yok."));
        if (!consent.isAwaitingAuthorization()) {
            throw new GkdPageException("Bu rıza onay beklemiyor",
                    "Rıza daha önce onaylanmış, reddedilmiş ya da iptal edilmiş (durum: " + consent.status() + ").");
        }
        if (consent.isAuthorizationExpired(OffsetDateTime.now(clock))) {
            throw new GkdPageException("Onay süresi doldu",
                    "Rızanın onaylanması için tanınan süre geçti. Lütfen işlemi uygulama üzerinden yeniden başlatın.");
        }
        if (consent.redirectUrl() == null) {
            throw new GkdPageException("Yönlendirme adresi yok", "Bu rıza yönlendirmeli GKD için oluşturulmamış.");
        }
        return consent;
    }

    private String rejectAndReturnToTpp(GkdConsent consent, String reasonCode, HttpServletRequest request) {
        events.publish(new GkdRejected(UUID.randomUUID().toString(), consent.consentId(),
                OffsetDateTime.now(clock), reasonCode));
        endGkdSession(request);
        return "redirect:" + tppRedirect(consent, "I", null);
    }

    private static String tppRedirect(GkdConsent consent, String status, String authorizationCode) {
        UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(consent.redirectUrl())
                .queryParam("rizaNo", consent.consentId())
                .queryParam("rizaTip", "H")
                .queryParam("rizaDrm", status);
        if (authorizationCode != null) {
            uri.queryParam("yetKod", authorizationCode);
        }
        return uri.encode().build().toUriString();
    }

    /** One GKD per login: the next consent needs a fresh Keycloak authentication. */
    private static void endGkdSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
    }

    private static void populate(Model model, GkdConsent consent, OidcUser customer) {
        model.addAttribute("consent", consent);
        model.addAttribute("customerName", Optional.ofNullable(customer.getFullName()).orElse(customer.getPreferredUsername()));
        model.addAttribute("permissions", consent.permissions().stream()
                .map(code -> code + " · " + PERMISSION_LABELS.getOrDefault(code, "Bilinmeyen izin"))
                .toList());
    }

    private static String message(Model model, String title, String detail) {
        model.addAttribute("title", title);
        model.addAttribute("detail", detail);
        return "gkd/message";
    }

    static final class GkdPageException extends RuntimeException {
        private final String title;

        GkdPageException(String title, String detail) {
            super(detail);
            this.title = title;
        }

        String title() {
            return title;
        }
    }
}
