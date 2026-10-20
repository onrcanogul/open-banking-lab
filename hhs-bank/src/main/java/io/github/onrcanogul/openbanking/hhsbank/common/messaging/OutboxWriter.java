package io.github.onrcanogul.openbanking.hhsbank.common.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OutboxWriter {

    private final JdbcTemplate jdbc;
    private final JsonMapper json;          // Boot 4 / Jackson 3. Boot 3'teysen ObjectMapper

    @Transactional(propagation = Propagation.MANDATORY)
    public void append(String aggregateType, String aggregateId, String topic, DomainEvent event) {
        try {
            jdbc.update("""
                INSERT INTO outbox_event (id, aggregate_type, aggregate_id, event_type, topic, payload, request_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                    event.eventId(), aggregateType, aggregateId,
                    event.getClass().getSimpleName(), topic,
                    json.writeValueAsString(event), MDC.get("requestId"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
