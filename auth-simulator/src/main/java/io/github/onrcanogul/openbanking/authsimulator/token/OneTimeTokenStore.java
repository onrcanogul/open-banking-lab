package io.github.onrcanogul.openbanking.authsimulator.token;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Single-use opaque tokens (yetKod, yenileme belirteci). Only a SHA-256 of the token is kept, and consuming
 * removes it atomically, so a replayed or concurrently reused token finds nothing.
 * In-memory: a restart forgets every outstanding token, which is acceptable for the lab.
 */
final class OneTimeTokenStore<T> {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final Map<String, Entry<T>> entries = new ConcurrentHashMap<>();
    private final Clock clock;

    OneTimeTokenStore(Clock clock) {
        this.clock = clock;
    }

    String issue(T grant, Instant expiresAt) {
        purgeExpired();
        byte[] raw = new byte[32];
        RANDOM.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        entries.put(hash(token), new Entry<>(grant, expiresAt));
        return token;
    }

    Optional<T> consume(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(entries.remove(hash(token)))
                .filter(e -> clock.instant().isBefore(e.expiresAt()))
                .map(Entry::grant);
    }

    private void purgeExpired() {
        Instant now = clock.instant();
        entries.values().removeIf(e -> !now.isBefore(e.expiresAt()));
    }

    private static String hash(String token) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private record Entry<T>(T grant, Instant expiresAt) {}
}
