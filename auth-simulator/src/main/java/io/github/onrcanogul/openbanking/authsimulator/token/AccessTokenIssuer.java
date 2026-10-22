package io.github.onrcanogul.openbanking.authsimulator.token;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import io.github.onrcanogul.openbanking.authsimulator.config.SimulatorProperties;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Signs the erişim belirteci hhs-bank accepts on X-Access-Token (iss, aud, scope, riza_no).
 * The RSA key lives only in memory: a restart rotates it and invalidates every token issued before.
 */
@Component
public class AccessTokenIssuer {

    static final String SCOPE = "hesap_bilgisi";

    private final RSAKey signingKey;
    private final JwtEncoder encoder;
    private final SimulatorProperties props;

    public AccessTokenIssuer(SimulatorProperties props) {
        this.props = props;
        try {
            this.signingKey = new RSAKeyGenerator(2048).keyID(UUID.randomUUID().toString()).generate();
        } catch (JOSEException e) {
            throw new IllegalStateException("Could not generate the token signing key", e);
        }
        this.encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(signingKey)));
    }

    public String issue(String consentId, String tppCode, String customerId, Instant issuedAt, Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.issuer())
                .audience(List.of(props.audience()))
                .subject(customerId)
                .id(UUID.randomUUID().toString())
                .issuedAt(issuedAt)
                .notBefore(issuedAt)
                .expiresAt(expiresAt)
                .claim("azp", tppCode)
                .claim("scope", SCOPE)
                .claim("riza_no", consentId)
                .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(signingKey.getKeyID()).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public Map<String, Object> publicJwks() {
        return new JWKSet(signingKey.toPublicJWK()).toJSONObject();
    }
}
