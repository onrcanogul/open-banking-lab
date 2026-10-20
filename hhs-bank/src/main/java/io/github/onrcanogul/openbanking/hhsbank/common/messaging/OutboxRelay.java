package io.github.onrcanogul.openbanking.hhsbank.common.messaging;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
class OutboxRelay {

    private static final int BATCH_SIZE = 100;
    private final JdbcTemplate jdbc;
    private final KafkaTemplate<String, String> kafka;

    @Scheduled(fixedDelayString = "${ohvps.outbox.poll-interval:1s}")
    @SchedulerLock(name = "outboxRelay", lockAtMostFor = "PT30S")
    public void publishPending() {
        List<OutboxRow> batch = jdbc.query("""
            SELECT id, aggregate_id, topic, payload, request_id
            FROM outbox_event
            WHERE published_at IS NULL
            ORDER BY created_at
            FETCH FIRST ? ROWS ONLY
            """, OutboxRow.MAPPER, BATCH_SIZE);

        for (OutboxRow row : batch) {
            try {
                ProducerRecord<String, String> record =
                        new ProducerRecord<>(row.topic(), row.aggregateId(), row.payload());
                record.headers().add("eventId", row.id().getBytes(StandardCharsets.UTF_8));
                if (row.requestId() != null) {
                    record.headers().add("X-Request-ID", row.requestId().getBytes(StandardCharsets.UTF_8));
                }

                kafka.send(record).get(5, TimeUnit.SECONDS);

                jdbc.update("UPDATE outbox_event SET published_at = SYSTIMESTAMP WHERE id = ?", row.id());
            } catch (Exception e) {
                jdbc.update("UPDATE outbox_event SET attempt_count = attempt_count + 1, last_error = ? WHERE id = ?",
                        StringUtils.truncate(String.valueOf(e.getMessage()), 1000), row.id());
                break;
            }
        }
    }
}