package io.github.onrcanogul.openbanking.hhsbank.api.error;

import io.github.onrcanogul.openbanking.hhsbank.common.error.DomainErrorCodes;
import io.github.onrcanogul.openbanking.hhsbank.common.error.OhvpsErrorFactory;
import io.github.onrcanogul.openbanking.hhsbank.common.error.OhvpsErrorResponse;
import io.github.onrcanogul.openbanking.hhsbank.common.error.OhvpsErrorResponse.FieldError;
import io.github.onrcanogul.openbanking.hhsbank.common.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

/**
 * Turns every failure that reaches Spring MVC into the ÖHVPS error object. Internal exception messages are
 * logged (with the error id) but never returned. Spring MVC's own exceptions (405, 406, 415, 404, bad JSON ...)
 * are covered by the base class and funnelled through {@link #handleExceptionInternal}.
 */
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class OhvpsExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String SORT_PARAMETER = "srlmKrtr";

    private final OhvpsErrorFactory factory;

    // Security exceptions belong to Spring Security's entry point / access denied handler, not to this advice.
    @ExceptionHandler({AccessDeniedException.class, AuthenticationException.class})
    void rethrowSecurityExceptions(Exception ex) throws Exception {
        throw ex;
    }

    @ExceptionHandler({
            OhvpsException.class, ConsentNotFoundException.class, ConsentNotActiveException.class,
            ConsentRevokedException.class, ConsentPermissionException.class, AccountNotFoundException.class,
            AccountNotInConsentException.class, InvalidConsentTransitionException.class})
    ResponseEntity<Object> handleDomain(RuntimeException ex, HttpServletRequest request) {
        OhvpsErrorCode code = DomainErrorCodes.of(ex).orElse(OhvpsErrorCode.INTERNAL_ERROR);
        return respond(request, code, List.of(), new HttpHeaders(), ex);
    }

    @ExceptionHandler(InvalidPagingException.class)
    ResponseEntity<Object> handleInvalidPaging(InvalidPagingException ex, HttpServletRequest request) {
        // the exception message is the offending query parameter name (syfNo, syfKytSayi, srlmYon)
        return respond(request, OhvpsErrorCode.INVALID_FORMAT,
                List.of(FieldError.invalid(null, ex.getMessage())), new HttpHeaders(), ex);
    }

    @ExceptionHandler(InvalidSortFieldException.class)
    ResponseEntity<Object> handleInvalidSortField(InvalidSortFieldException ex, HttpServletRequest request) {
        return respond(request, OhvpsErrorCode.INVALID_FORMAT,
                List.of(FieldError.invalid(null, SORT_PARAMETER)), new HttpHeaders(), ex);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Object> handleUnexpected(Exception ex, HttpServletRequest request) {
        return respond(request, OhvpsErrorCode.INTERNAL_ERROR, List.of(), new HttpHeaders(), ex);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Object target = ex.getBindingResult().getTarget();
        Class<?> root = target == null ? null : target.getClass();
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> toFieldError(root, error))
                .toList();
        return respond(servlet(request), OhvpsErrorCode.INVALID_FORMAT, fieldErrors, headers, ex);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return respond(servlet(request), OhvpsErrorCode.INVALID_FORMAT,
                List.of(FieldError.missing(null, ex.getParameterName())), headers, ex);
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldError> fieldErrors = ex instanceof MissingRequestHeaderException header
                ? List.of(FieldError.missing(null, header.getHeaderName()))
                : List.of();
        return respond(servlet(request), OhvpsErrorCode.INVALID_FORMAT, fieldErrors, headers, ex);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String field = ex instanceof MethodArgumentTypeMismatchException mismatch
                ? mismatch.getName()
                : ex.getPropertyName();
        List<FieldError> fieldErrors = field == null ? List.of() : List.of(FieldError.invalid(null, field));
        return respond(servlet(request), OhvpsErrorCode.INVALID_FORMAT, fieldErrors, headers, ex);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
            HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        // 404, 405, 406, 415, unreadable body ...: keep the headers the base class computed (Allow, Accept)
        return respond(servlet(request), DomainErrorCodes.forStatus(statusCode), List.of(), headers, ex);
    }

    private ResponseEntity<Object> respond(HttpServletRequest request, OhvpsErrorCode code,
                                           List<FieldError> fieldErrors, HttpHeaders headers, Throwable cause) {
        OhvpsErrorResponse body = factory.build(request.getRequestURI(), code, fieldErrors);
        if (code.status().is5xxServerError()) {
            log.error("Request failed: errorId={} code={}", body.id(), code.code(), cause);
        } else {
            log.info("Request rejected: errorId={} code={} cause={}", body.id(), code.code(), cause.getClass().getSimpleName());
            log.debug("Rejection detail for errorId={}: {}", body.id(), cause.getMessage());
        }
        return ResponseEntity.status(code.status())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    private static FieldError toFieldError(Class<?> root, org.springframework.validation.FieldError error) {
        String field = root == null ? error.getField() : JsonFieldNames.resolve(root, error.getField());
        String constraint = error.getCode() == null ? "" : error.getCode();
        return switch (constraint) {
            case "NotNull", "NotBlank", "NotEmpty" -> FieldError.missing(error.getObjectName(), field);
            default -> FieldError.invalid(error.getObjectName(), field);
        };
    }

    private static HttpServletRequest servlet(WebRequest request) {
        return ((ServletWebRequest) request).getRequest();
    }
}
