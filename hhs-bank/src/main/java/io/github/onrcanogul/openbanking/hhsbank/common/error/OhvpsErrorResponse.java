package io.github.onrcanogul.openbanking.hhsbank.common.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;

/** ÖHVPS error object (spec 3.x): the only error shape clients ever see. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OhvpsErrorResponse(
        String path,
        String id,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") OffsetDateTime timestamp,
        int httpCode,
        String httpMessage,
        String moreInformation,
        String moreInformationTr,
        String errorCode,
        List<FieldError> fieldErrors) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record FieldError(String objectName, String field, String messageTr, String message, String code) {

        public static final String CODE_MISSING = "TR.OHVPS.Field.Missing";
        public static final String CODE_INVALID = "TR.OHVPS.Field.Invalid";

        public static FieldError missing(String objectName, String field) {
            return new FieldError(objectName, field, "Zorunlu alan eksik", "Field is required", CODE_MISSING);
        }

        public static FieldError invalid(String objectName, String field) {
            return new FieldError(objectName, field, "Alan değeri geçersiz", "Field value is invalid", CODE_INVALID);
        }
    }
}
