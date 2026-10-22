package io.github.onrcanogul.openbanking.authsimulator.messaging;

import io.github.onrcanogul.openbanking.authsimulator.config.SimulatorProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Publishes synchronously: the customer is redirected (or the token returned) only after Kafka acknowledged
 * the event. The simulator has no database, so there is no outbox; a failed send surfaces as an error instead.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GkdEventPublisher {

    private static final long SEND_TIMEOUT_SECONDS = 5;

    private final KafkaTemplate<String, String> kafka;
    private final JsonMapper json;
    private final SimulatorProperties props;

    public void publish(GkdEvent event) {
        ProducerRecord<String, String> record = new ProducerRecord<>(
                props.gkdEventsTopic(), event.consentId(), json.writerFor(GkdEvent.class).writeValueAsString(event));
        String requestId = MDC.get("requestId");
        if (requestId != null) {
            record.headers().add("X-Request-ID", requestId.getBytes(StandardCharsets.UTF_8));
        }

        try {
            kafka.send(record).get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GkdEventPublishException(event, e);
        } catch (ExecutionException | TimeoutException e) {
            throw new GkdEventPublishException(event, e);
        }
        log.info("Published {} for consent {}", event.getClass().getSimpleName(), event.consentId());
    }

    public static class GkdEventPublishException extends RuntimeException {
        GkdEventPublishException(GkdEvent event, Throwable cause) {
            super("Could not publish " + event.getClass().getSimpleName() + " for consent " + event.consentId(), cause);
        }
    }
}
