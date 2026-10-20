package io.github.onrcanogul.openbanking.hhsbank.common.exception;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public class InvalidEventPayloadException extends RuntimeException {
    public InvalidEventPayloadException(ConsumerRecord<String, String> record, String unparseablePayload, RuntimeException e) {
    }
}
