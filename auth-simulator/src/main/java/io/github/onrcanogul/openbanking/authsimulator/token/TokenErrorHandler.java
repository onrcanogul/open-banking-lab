package io.github.onrcanogul.openbanking.authsimulator.token;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.onrcanogul.openbanking.authsimulator.messaging.GkdEventPublisher.GkdEventPublishException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import java.time.OffsetDateTime;
import java.util.UUID;

import static io.github.onrcanogul.openbanking.authsimulator.token.OhvpsTokenException.Error.INVALID_FORMAT;

/** Same error object shape hhs-bank returns (ÖHVPS hata nesnesi). */
@RestControllerAdvice(assignableTypes = TokenController.class)
@Slf4j
class TokenErrorHandler {

    @ExceptionHandler(OhvpsTokenException.class)
    ResponseEntity<ErrorBody> handle(OhvpsTokenException ex, HttpServletRequest request) {
        log.info("Token request rejected: {} ({})", ex.error().code(), ex.getMessage());
        return body(request, ex.error().status(), ex.error().code(), ex.getMessage(), ex.error().messageTr());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ErrorBody> invalid(Exception ex, HttpServletRequest request) {
        return body(request, INVALID_FORMAT.status(), INVALID_FORMAT.code(), "Validation error", INVALID_FORMAT.messageTr());
    }

    @ExceptionHandler({GkdEventPublishException.class, RestClientException.class})
    ResponseEntity<ErrorBody> unavailable(RuntimeException ex, HttpServletRequest request) {
        log.warn("Token request failed on a downstream dependency", ex);
        return body(request, HttpStatus.SERVICE_UNAVAILABLE, "TR.OHVPS.Server.ServiceUnavailable",
                "Service unavailable", "Hizmet şu anda verilemiyor");
    }

    private static ResponseEntity<ErrorBody> body(HttpServletRequest request, HttpStatus status, String code,
                                                  String message, String messageTr) {
        return ResponseEntity.status(status).body(new ErrorBody(
                request.getRequestURI(), UUID.randomUUID().toString(), OffsetDateTime.now(),
                status.value(), status.getReasonPhrase(), message, messageTr, code));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record ErrorBody(String path, String id,
                     @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") OffsetDateTime timestamp,
                     int httpCode, String httpMessage, String moreInformation, String moreInformationTr,
                     String errorCode) {}
}
