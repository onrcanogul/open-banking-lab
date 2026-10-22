package io.github.onrcanogul.openbanking.authsimulator.token;

import org.springframework.http.HttpStatus;

public class OhvpsTokenException extends RuntimeException {

    public enum Error {
        INVALID_FORMAT("TR.OHVPS.Resource.InvalidFormat", HttpStatus.BAD_REQUEST, "Şema kontrolleri başarısız"),
        INVALID_ASPSP("TR.OHVPS.Connection.InvalidASPSP", HttpStatus.BAD_REQUEST, "Geçersiz HHS kodu"),
        INVALID_TPP("TR.OHVPS.Connection.InvalidTPP", HttpStatus.BAD_REQUEST, "Geçersiz YÖS kodu"),
        INVALID_TOKEN("TR.OHVPS.Connection.InvalidToken", HttpStatus.UNAUTHORIZED, "Geçersiz veya süresi dolmuş belirteç"),
        CONSENT_MISMATCH("TR.OHVPS.Resource.ConsentMismatch", HttpStatus.FORBIDDEN, "Rıza durumu işleme uygun değil"),
        CONSENT_REVOKED("TR.OHVPS.Resource.ConsentRevoked", HttpStatus.FORBIDDEN, "Rıza iptal edilmiş veya sonlandırılmış");

        private final String code;
        private final HttpStatus status;
        private final String messageTr;

        Error(String code, HttpStatus status, String messageTr) {
            this.code = code;
            this.status = status;
            this.messageTr = messageTr;
        }

        public String code() { return code; }
        public HttpStatus status() { return status; }
        public String messageTr() { return messageTr; }
    }

    private final Error error;

    public OhvpsTokenException(Error error, String message) {
        super(message);
        this.error = error;
    }

    public Error error() {
        return error;
    }
}
