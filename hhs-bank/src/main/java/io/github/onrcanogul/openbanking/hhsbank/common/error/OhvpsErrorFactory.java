package io.github.onrcanogul.openbanking.hhsbank.common.error;

import io.github.onrcanogul.openbanking.hhsbank.common.error.OhvpsErrorResponse.FieldError;
import io.github.onrcanogul.openbanking.hhsbank.common.exception.OhvpsErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Single place that turns an {@link OhvpsErrorCode} into the wire object. Clients only get the catalogued
 * messages; internal exception messages never reach this class. {@code id} identifies this occurrence and is
 * logged next to the stack trace so support can correlate a client report with the server log.
 */
@Component
@RequiredArgsConstructor
public class OhvpsErrorFactory {

    private final Clock clock;

    public OhvpsErrorResponse build(String path, OhvpsErrorCode code, List<FieldError> fieldErrors) {
        return new OhvpsErrorResponse(
                path,
                UUID.randomUUID().toString(),
                OffsetDateTime.now(clock),
                code.status().value(),
                code.status().getReasonPhrase(),
                code.defaultMessage(),
                code.defaultMessageTr(),
                code.code(),
                fieldErrors == null || fieldErrors.isEmpty() ? null : List.copyOf(fieldErrors));
    }
}
