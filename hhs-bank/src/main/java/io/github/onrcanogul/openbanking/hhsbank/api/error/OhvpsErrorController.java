package io.github.onrcanogul.openbanking.hhsbank.api.error;

import io.github.onrcanogul.openbanking.hhsbank.common.error.DomainErrorCodes;
import io.github.onrcanogul.openbanking.hhsbank.common.error.OhvpsErrorFactory;
import io.github.onrcanogul.openbanking.hhsbank.common.error.OhvpsErrorResponse;
import io.github.onrcanogul.openbanking.hhsbank.common.exception.OhvpsErrorCode;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Replaces Spring Boot's {@code BasicErrorController}: failures that never reach Spring MVC (exceptions in
 * filters, container-level errors) are forwarded to /error by the servlet container and would otherwise be
 * answered with Boot's own JSON shape instead of the ÖHVPS error object.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
class OhvpsErrorController implements ErrorController {

    private final OhvpsErrorFactory factory;

    @RequestMapping("${server.error.path:${error.path:/error}}")
    ResponseEntity<OhvpsErrorResponse> error(HttpServletRequest request) {
        Object statusAttribute = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        HttpStatusCode status = statusAttribute instanceof Integer value
                ? HttpStatusCode.valueOf(value)
                : HttpStatus.INTERNAL_SERVER_ERROR;
        OhvpsErrorCode code = DomainErrorCodes.forStatus(status);

        Object originalPath = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        OhvpsErrorResponse body = factory.build(
                originalPath instanceof String path ? path : request.getRequestURI(), code, List.of());

        if (code.status().is5xxServerError()) {
            log.error("Container-level failure: errorId={} code={}", body.id(), code.code(),
                    request.getAttribute(RequestDispatcher.ERROR_EXCEPTION));
        } else {
            log.info("Container-level rejection: errorId={} code={}", body.id(), code.code());
        }
        return ResponseEntity.status(code.status()).contentType(MediaType.APPLICATION_JSON).body(body);
    }
}
