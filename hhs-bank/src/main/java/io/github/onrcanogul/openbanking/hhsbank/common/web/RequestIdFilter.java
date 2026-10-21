package io.github.onrcanogul.openbanking.hhsbank.common.web;

import io.github.onrcanogul.openbanking.hhsbank.common.error.OhvpsErrorResponse.FieldError;
import io.github.onrcanogul.openbanking.hhsbank.common.error.OhvpsErrorWriter;
import io.github.onrcanogul.openbanking.hhsbank.common.exception.OhvpsErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * X-Request-ID is mandatory on every ÖHVPS call (AN1..36) and is echoed back in the response (spec Table 2/3).
 * Runs before Spring Security so that 401/403 answers carry the header too, and publishes the id to the
 * logging MDC (and, through it, to the outbox).
 *
 * The spec only says "alphanumeric"; hyphens are accepted because YÖS clients send UUIDs.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Request-ID";
    public static final String MDC_KEY = "requestId";

    private static final String API_PREFIX = "/ohvps/";
    private static final Pattern VALID = Pattern.compile("[A-Za-z0-9-]{1,36}");

    private final OhvpsErrorWriter errorWriter;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(API_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String requestId = request.getHeader(HEADER);
        if (requestId == null || !VALID.matcher(requestId).matches()) {
            // The spec's example answers an empty X-Request-ID with Field.Invalid; absent is treated the same.
            errorWriter.write(request, response, OhvpsErrorCode.INVALID_FORMAT,
                    List.of(FieldError.invalid(null, HEADER)));
            return;
        }

        response.setHeader(HEADER, requestId);
        MDC.put(MDC_KEY, requestId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
