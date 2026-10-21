package io.github.onrcanogul.openbanking.hhsbank.security;

import io.github.onrcanogul.openbanking.hhsbank.common.error.OhvpsErrorWriter;
import io.github.onrcanogul.openbanking.hhsbank.common.exception.OhvpsErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OhvpsSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final OhvpsErrorWriter writer;

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res,
                         AuthenticationException ex) throws IOException {
        OhvpsErrorCode code = ex instanceof OhvpsAuthenticationException known
                ? known.errorCode()
                : OhvpsErrorCode.INVALID_TOKEN;
        if (code.status() == HttpStatus.UNAUTHORIZED) {
            res.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");       // RFC 6750: required on a 401
        }
        log.info("Authentication rejected: {} ({})", code.code(), ex.getClass().getSimpleName());
        writer.write(req, res, code, List.of());
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res,
                       AccessDeniedException ex) throws IOException {
        log.info("Access denied: {}", OhvpsErrorCode.FORBIDDEN.code());
        writer.write(req, res, OhvpsErrorCode.FORBIDDEN, List.of());
    }
}
