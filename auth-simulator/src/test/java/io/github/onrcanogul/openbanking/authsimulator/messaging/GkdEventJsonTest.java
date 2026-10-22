package io.github.onrcanogul.openbanking.authsimulator.messaging;

import io.github.onrcanogul.openbanking.authsimulator.messaging.GkdEvent.AccessTokenIssued;
import io.github.onrcanogul.openbanking.authsimulator.messaging.GkdEvent.GkdApproved;
import io.github.onrcanogul.openbanking.authsimulator.messaging.GkdEvent.GkdRejected;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.OffsetDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** Pins the wire format hhs-bank's ConsentAuthEvent (@JsonTypeInfo NAME, property eventType) deserializes. */
class GkdEventJsonTest {

    private final JsonMapper json = JsonMapper.builder().build();
    private final OffsetDateTime at = OffsetDateTime.parse("2026-10-21T21:00:00+03:00");

    @Test
    void approvedCarriesTypeAndSelectedAccounts() {
        JsonNode node = write(new GkdApproved("e1", "RIZA-1", at, Set.of("ACC-1")));

        assertThat(node.get("eventType").asString()).isEqualTo("GkdApproved");
        assertThat(node.get("eventId").asString()).isEqualTo("e1");
        assertThat(node.get("consentId").asString()).isEqualTo("RIZA-1");
        assertThat(node.get("occurredAt").asString()).isEqualTo("2026-10-21T21:00:00+03:00");
        assertThat(node.get("selectedAccountRefs").get(0).asString()).isEqualTo("ACC-1");
    }

    @Test
    void rejectedAndTokenIssuedUseTheirSimpleNames() {
        JsonNode rejected = write(new GkdRejected("e2", "RIZA-1", at, "13"));
        assertThat(rejected.get("eventType").asString()).isEqualTo("GkdRejected");
        assertThat(rejected.get("cancellationReasonCode").asString()).isEqualTo("13");

        assertThat(write(new AccessTokenIssued("e3", "RIZA-1", at)).get("eventType").asString())
                .isEqualTo("AccessTokenIssued");
    }

    private JsonNode write(GkdEvent event) {
        return json.readTree(json.writerFor(GkdEvent.class).writeValueAsString(event));
    }
}
