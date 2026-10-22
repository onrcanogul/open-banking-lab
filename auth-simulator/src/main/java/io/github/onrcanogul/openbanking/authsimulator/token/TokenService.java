package io.github.onrcanogul.openbanking.authsimulator.token;

import io.github.onrcanogul.openbanking.authsimulator.config.SimulatorProperties;
import io.github.onrcanogul.openbanking.authsimulator.consent.ConsentLookup;
import io.github.onrcanogul.openbanking.authsimulator.consent.GkdConsent;
import io.github.onrcanogul.openbanking.authsimulator.messaging.GkdEvent.AccessTokenIssued;
import io.github.onrcanogul.openbanking.authsimulator.messaging.GkdEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

import static io.github.onrcanogul.openbanking.authsimulator.token.OhvpsTokenException.Error.*;

/**
 * yetKod → erişim belirteci (+ AccessTokenIssued, which moves the consent Y → K in hhs-bank) and
 * yenileme belirteci rotation. Refresh tokens live until the consent's erisimIzniSonTrh; access tokens
 * never outlive the consent either.
 */
@Service
public class TokenService {

    private final ConsentLookup consents;
    private final AccessTokenIssuer issuer;
    private final GkdEventPublisher events;
    private final SimulatorProperties props;
    private final Clock clock;
    private final OneTimeTokenStore<ConsentGrant> authorizationCodes;
    private final OneTimeTokenStore<ConsentGrant> refreshTokens;

    public TokenService(ConsentLookup consents, AccessTokenIssuer issuer, GkdEventPublisher events,
                        SimulatorProperties props, Clock clock) {
        this.consents = consents;
        this.issuer = issuer;
        this.events = events;
        this.props = props;
        this.clock = clock;
        this.authorizationCodes = new OneTimeTokenStore<>(clock);
        this.refreshTokens = new OneTimeTokenStore<>(clock);
    }

    /** Called by the GKD screen right after the customer approved and GkdApproved was published. */
    public String issueAuthorizationCode(GkdConsent consent) {
        return authorizationCodes.issue(
                new ConsentGrant(consent.consentId(), consent.tppCode(), consent.customerId()),
                clock.instant().plus(props.authorizationCodeTtl()));
    }

    public TokenResponse exchange(TokenRequest request, String authenticatedTppCode) {
        if (!TokenRequest.ACCOUNT_INFO_CONSENT.equals(request.consentType())) {
            throw new OhvpsTokenException(INVALID_FORMAT, "Only rizaTip=H is supported");
        }
        if (!props.aspspCode().equals(request.aspspCode())) {
            throw new OhvpsTokenException(INVALID_ASPSP, "hhsKod mismatch");
        }
        if (!request.tppCode().equalsIgnoreCase(authenticatedTppCode)) {
            throw new OhvpsTokenException(INVALID_TPP, "yosKod does not match the authenticated TPP");
        }

        return switch (request.grantType()) {
            case TokenRequest.AUTHORIZATION_CODE -> fromAuthorizationCode(request);
            case TokenRequest.REFRESH_TOKEN -> fromRefreshToken(request);
            default -> throw new OhvpsTokenException(INVALID_FORMAT, "Unsupported yetTip");
        };
    }

    private TokenResponse fromAuthorizationCode(TokenRequest request) {
        ConsentGrant grant = authorizationCodes.consume(request.code())
                .filter(g -> g.matches(request.consentId(), request.tppCode()))
                .orElseThrow(() -> new OhvpsTokenException(INVALID_TOKEN, "Unknown, used or expired yetKod"));
        GkdConsent consent = load(grant);

        // B is fine: a yetKod exists only after GkdApproved was published, and that event shares the consent's
        // partition with AccessTokenIssued, so hhs-bank applies Y before K.
        if (!consent.isAwaitingAuthorization() && !GkdConsent.AUTHORIZED.equals(consent.status())) {
            throw statusError(consent);
        }

        events.publish(new AccessTokenIssued(UUID.randomUUID().toString(), consent.consentId(), OffsetDateTime.now(clock)));
        return issueTokens(grant, consent);
    }

    private TokenResponse fromRefreshToken(TokenRequest request) {
        ConsentGrant grant = refreshTokens.consume(request.code())
                .filter(g -> g.matches(request.consentId(), request.tppCode()))
                .orElseThrow(() -> new OhvpsTokenException(INVALID_TOKEN, "Unknown, used or expired yenileme belirteci"));
        GkdConsent consent = load(grant);

        if (!GkdConsent.AUTHORIZATION_USED.equals(consent.status())) {
            throw statusError(consent);
        }
        return issueTokens(grant, consent);
    }

    private GkdConsent load(ConsentGrant grant) {
        GkdConsent consent = consents.find(grant.consentId())
                .orElseThrow(() -> new OhvpsTokenException(CONSENT_MISMATCH, "Consent not found"));
        if (!consent.tppCode().equalsIgnoreCase(grant.tppCode())) {
            throw new OhvpsTokenException(CONSENT_MISMATCH, "Consent belongs to another TPP");
        }
        return consent;
    }

    private TokenResponse issueTokens(ConsentGrant grant, GkdConsent consent) {
        Instant now = clock.instant();
        Instant consentEnd = consent.expiresAt().toInstant();
        if (!now.isBefore(consentEnd)) {
            throw new OhvpsTokenException(CONSENT_MISMATCH, "Consent access period has ended");
        }

        Instant accessEnd = min(now.plus(props.accessTokenTtl()), consentEnd);
        String accessToken = issuer.issue(grant.consentId(), grant.tppCode(), grant.customerId(), now, accessEnd);
        String refreshToken = refreshTokens.issue(grant, consentEnd);

        return new TokenResponse(accessToken, Duration.between(now, accessEnd).toSeconds(),
                refreshToken, Duration.between(now, consentEnd).toSeconds());
    }

    private static OhvpsTokenException statusError(GkdConsent consent) {
        boolean closed = "S".equals(consent.status()) || "I".equals(consent.status());
        return new OhvpsTokenException(closed ? CONSENT_REVOKED : CONSENT_MISMATCH,
                "Consent status " + consent.status() + " does not allow this grant");
    }

    private static Instant min(Instant a, Instant b) {
        return a.isBefore(b) ? a : b;
    }
}
