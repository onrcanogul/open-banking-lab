package io.github.onrcanogul.openbanking.authsimulator.token;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import io.github.onrcanogul.openbanking.authsimulator.config.SimulatorProperties;
import io.github.onrcanogul.openbanking.authsimulator.consent.GkdConsent;
import io.github.onrcanogul.openbanking.authsimulator.messaging.GkdEvent;
import io.github.onrcanogul.openbanking.authsimulator.messaging.GkdEvent.AccessTokenIssued;
import io.github.onrcanogul.openbanking.authsimulator.messaging.GkdEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.time.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.github.onrcanogul.openbanking.authsimulator.token.OhvpsTokenException.Error.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TokenServiceTest {

    private static final String CONSENT_ID = "RIZA-42";
    private static final String TPP = "TPP1";

    private final MutableClock clock = new MutableClock(Instant.now());
    private final Map<String, GkdConsent> consents = new HashMap<>();
    private final GkdEventPublisher events = mock(GkdEventPublisher.class);
    private final SimulatorProperties props = new SimulatorProperties(
            "HHS1", "http://localhost:8082", "ohvps-hbh", Duration.ofHours(1), Duration.ofMinutes(5),
            "http://localhost:8081", "ohvps.gkd.events");
    private final AccessTokenIssuer issuer = new AccessTokenIssuer(props);

    private TokenService service;

    @BeforeEach
    void setUp() {
        service = new TokenService(id -> Optional.ofNullable(consents.get(id)), issuer, events, props, clock);
    }

    @Test
    void authorizationCodeIssuesConsentBoundTokensAndMarksTheConsentUsed() throws Exception {
        GkdConsent consent = givenConsent(GkdConsent.AUTHORIZED, Duration.ofDays(30));
        String code = service.issueAuthorizationCode(consent);

        TokenResponse response = service.exchange(request(TokenRequest.AUTHORIZATION_CODE, code), TPP);

        Jwt jwt = decode(response.accessToken());
        assertThat(jwt.getClaimAsString("riza_no")).isEqualTo(CONSENT_ID);
        assertThat(jwt.getAudience()).containsExactly("ohvps-hbh");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("http://localhost:8082");
        assertThat(jwt.getClaimAsString("scope")).isEqualTo("hesap_bilgisi");
        assertThat(jwt.getSubject()).isEqualTo("CUST-1");
        assertThat(response.expiresInSeconds()).isEqualTo(3600);
        assertThat(response.refreshToken()).isNotBlank();

        ArgumentCaptor<GkdEvent> event = ArgumentCaptor.forClass(GkdEvent.class);
        verify(events).publish(event.capture());
        assertThat(event.getValue()).isInstanceOf(AccessTokenIssued.class);
        assertThat(event.getValue().consentId()).isEqualTo(CONSENT_ID);
    }

    @Test
    void approvalStillInFlightIsAccepted() {
        GkdConsent consent = givenConsent(GkdConsent.AWAITING_AUTHORIZATION, Duration.ofDays(30));
        String code = service.issueAuthorizationCode(consent);

        assertThat(service.exchange(request(TokenRequest.AUTHORIZATION_CODE, code), TPP).accessToken()).isNotBlank();
    }

    @Test
    void authorizationCodeIsSingleUse() {
        String code = service.issueAuthorizationCode(givenConsent(GkdConsent.AUTHORIZED, Duration.ofDays(30)));
        service.exchange(request(TokenRequest.AUTHORIZATION_CODE, code), TPP);

        assertRejected(() -> service.exchange(request(TokenRequest.AUTHORIZATION_CODE, code), TPP), INVALID_TOKEN);
        verify(events, times(1)).publish(any());
    }

    @Test
    void authorizationCodeExpires() {
        String code = service.issueAuthorizationCode(givenConsent(GkdConsent.AUTHORIZED, Duration.ofDays(30)));
        clock.advance(Duration.ofMinutes(5));

        assertRejected(() -> service.exchange(request(TokenRequest.AUTHORIZATION_CODE, code), TPP), INVALID_TOKEN);
        verifyNoInteractions(events);
    }

    @Test
    void authorizationCodeIsBoundToItsConsent() {
        String code = service.issueAuthorizationCode(givenConsent(GkdConsent.AUTHORIZED, Duration.ofDays(30)));
        TokenRequest otherConsent = new TokenRequest("HHS1", TPP, TokenRequest.AUTHORIZATION_CODE, code, "RIZA-OTHER", "H");

        assertRejected(() -> service.exchange(otherConsent, TPP), INVALID_TOKEN);
    }

    @Test
    void yosKodMustBeTheAuthenticatedTpp() {
        String code = service.issueAuthorizationCode(givenConsent(GkdConsent.AUTHORIZED, Duration.ofDays(30)));

        assertRejected(() -> service.exchange(request(TokenRequest.AUTHORIZATION_CODE, code), "TPP2"), INVALID_TPP);
    }

    @Test
    void hhsKodMustBeThisBank() {
        String code = service.issueAuthorizationCode(givenConsent(GkdConsent.AUTHORIZED, Duration.ofDays(30)));
        TokenRequest wrongBank = new TokenRequest("HHS9", TPP, TokenRequest.AUTHORIZATION_CODE, code, CONSENT_ID, "H");

        assertRejected(() -> service.exchange(wrongBank, TPP), INVALID_ASPSP);
    }

    @Test
    void refreshRotatesTheRefreshToken() {
        String code = service.issueAuthorizationCode(givenConsent(GkdConsent.AUTHORIZED, Duration.ofDays(30)));
        TokenResponse first = service.exchange(request(TokenRequest.AUTHORIZATION_CODE, code), TPP);
        givenConsent(GkdConsent.AUTHORIZATION_USED, Duration.ofDays(30));

        TokenResponse refreshed = service.exchange(request(TokenRequest.REFRESH_TOKEN, first.refreshToken()), TPP);

        assertThat(refreshed.accessToken()).isNotEqualTo(first.accessToken());
        assertThat(refreshed.refreshToken()).isNotEqualTo(first.refreshToken());
        assertRejected(() -> service.exchange(request(TokenRequest.REFRESH_TOKEN, first.refreshToken()), TPP), INVALID_TOKEN);
        verify(events, times(1)).publish(any());   // only the first exchange moves the consent to K
    }

    @Test
    void refreshIsRefusedOnceTheConsentIsRevoked() {
        String code = service.issueAuthorizationCode(givenConsent(GkdConsent.AUTHORIZED, Duration.ofDays(30)));
        TokenResponse first = service.exchange(request(TokenRequest.AUTHORIZATION_CODE, code), TPP);
        givenConsent("I", Duration.ofDays(30));

        assertRejected(() -> service.exchange(request(TokenRequest.REFRESH_TOKEN, first.refreshToken()), TPP), CONSENT_REVOKED);
    }

    @Test
    void tokensNeverOutliveTheConsent() {
        String code = service.issueAuthorizationCode(givenConsent(GkdConsent.AUTHORIZED, Duration.ofMinutes(10)));

        TokenResponse response = service.exchange(request(TokenRequest.AUTHORIZATION_CODE, code), TPP);

        assertThat(response.expiresInSeconds()).isEqualTo(600);
        assertThat(response.refreshExpiresInSeconds()).isEqualTo(600);
    }

    private GkdConsent givenConsent(String status, Duration accessPeriod) {
        OffsetDateTime now = OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.ofHours(3));
        GkdConsent consent = new GkdConsent(CONSENT_ID, status, TPP, "CUST-1",
                new GkdConsent.Identity("K", "11111111110", "B"), List.of("01", "03"),
                now.plus(accessPeriod), now.plusMinutes(5), "http://localhost:8083/callback",
                List.of(new GkdConsent.Account("ACC-1", "TR000000000000000000000001", "TRY")));
        consents.put(CONSENT_ID, consent);
        return consent;
    }

    private static TokenRequest request(String grantType, String code) {
        return new TokenRequest("HHS1", TPP, grantType, code, CONSENT_ID, "H");
    }

    private Jwt decode(String token) throws Exception {
        RSAKey key = JWKSet.parse(issuer.publicJwks()).getKeys().getFirst().toRSAKey();
        return NimbusJwtDecoder.withPublicKey(key.toRSAPublicKey()).build().decode(token);
    }

    private static void assertRejected(Runnable call, OhvpsTokenException.Error expected) {
        assertThatThrownBy(call::run)
                .isInstanceOfSatisfying(OhvpsTokenException.class, e -> assertThat(e.error()).isEqualTo(expected));
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) { this.now = now; }

        void advance(Duration d) { now = now.plus(d); }

        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }
}
