package io.github.onrcanogul.openbanking.hhsbank.common.messaging;

import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class OutboxWriter {

    private final JdbcTemplate jdbc;
    private final JsonMapper json;          // Boot 4 / Jackson 3: JacksonException unchecked, rolls back the transaction

    @Transactional(propagation = Propagation.MANDATORY)
    public void append(String aggregateType, String aggregateId, String topic, DomainEvent event) {
        jdbc.update("""
            INSERT INTO outbox_event (id, aggregate_type, aggregate_id, event_type, topic, payload, request_id)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """,
                event.eventId(), aggregateType, aggregateId,
                event.getClass().getSimpleName(), topic,
                json.writeValueAsString(event), MDC.get("requestId"));
    }
}
