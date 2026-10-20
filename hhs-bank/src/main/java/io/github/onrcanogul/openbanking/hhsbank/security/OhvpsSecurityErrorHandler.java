package io.github.onrcanogul.openbanking.hhsbank.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Clock;
import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class OhvpsSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper mapper;
    private final Clock clock;

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res,
                         AuthenticationException ex) throws IOException {
        res.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");       // RFC 6750: 401 yanıtında bulunmalı
        write(req, res, HttpStatus.UNAUTHORIZED, "TR.OHVPS.Connection.InvalidToken");
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res,
                       AccessDeniedException ex) throws IOException {
        write(req, res, HttpStatus.FORBIDDEN, "TR.OHVPS.Resource.Forbidden");
    }

    private void write(HttpServletRequest req, HttpServletResponse res,
                       HttpStatus status, String errorCode) throws IOException {
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(res.getOutputStream(), new OhvpsErrorResponse(
                req.getRequestURI(),
                req.getHeader("X-Request-ID"),
                OffsetDateTime.now(clock),
                status.value(),
                status.getReasonPhrase(),
                errorCode));
    }
}
