package io.github.onrcanogul.openbanking.hhsbank.infrastructure.messaging.listener;

import io.github.onrcanogul.openbanking.hhsbank.application.consent.ConsentAuthEventHandler;
import io.github.onrcanogul.openbanking.hhsbank.application.event.ConsentAuthEvent;
import io.github.onrcanogul.openbanking.hhsbank.common.exception.InvalidEventPayloadException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class GkdEventListener {
    private static final String CONSUMER = "bank-consent";

    private final JsonMapper jsonMapper;
    private final ConsentAuthEventHandler handler;

    @KafkaListener(topics = "${ohvps.messaging.topics.gkd-events}", groupId = CONSUMER)
    void onMessage(ConsumerRecord<String, String> record) {
        String requestId = header(record, "X-Request-ID");
        try {
            if (requestId != null) MDC.put("requestId", requestId);
            MDC.put("consentId", record.key());

            ConsentAuthEvent event = parse(record);

            if(!event.consentId().equals(record.key())) {
                throw new InvalidEventPayloadException(record, "Key does not match consentId", new RuntimeException());
            }

            handler.handle(event, CONSUMER);
        }
        finally {
            MDC.clear();
        }
    }

    private ConsentAuthEvent parse(ConsumerRecord<String, String> record) {
        try {
            return jsonMapper.readValue(record.value(), ConsentAuthEvent.class);
        } catch (JacksonException e) {
            throw new InvalidEventPayloadException(record, "Unparseable payload", e);
        }
    }
    private static String header(ConsumerRecord<?, ?> record, String name) {
        Header h = record.headers().lastHeader(name);
        return h == null ? null : new String(h.value(), StandardCharsets.UTF_8);
    }
}
