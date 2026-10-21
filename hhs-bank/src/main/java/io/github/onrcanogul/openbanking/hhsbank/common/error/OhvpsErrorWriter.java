package io.github.onrcanogul.openbanking.hhsbank.common.error;

import io.github.onrcanogul.openbanking.hhsbank.common.error.OhvpsErrorResponse.FieldError;
import io.github.onrcanogul.openbanking.hhsbank.common.exception.OhvpsErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Writes the error object straight to the servlet response, for code that runs outside Spring MVC (filters, security). */
@Component
@RequiredArgsConstructor
public class OhvpsErrorWriter {

    private final OhvpsErrorFactory factory;
    private final JsonMapper mapper;

    public void write(HttpServletRequest request, HttpServletResponse response,
                      OhvpsErrorCode code, List<FieldError> fieldErrors) throws IOException {
        OhvpsErrorResponse body = factory.build(request.getRequestURI(), code, fieldErrors);
        response.setStatus(code.status().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        mapper.writeValue(response.getOutputStream(), body);
    }
}
