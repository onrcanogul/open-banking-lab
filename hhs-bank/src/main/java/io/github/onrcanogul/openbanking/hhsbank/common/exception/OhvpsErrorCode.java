package io.github.onrcanogul.openbanking.hhsbank.common.exception;

import org.springframework.http.HttpStatus;

// common/error/OhvpsErrorCode.java
public enum OhvpsErrorCode {

    // ── Resource: isteğin şekli veya hedef kaynakla ilgili ──
    INVALID_FORMAT("TR.OHVPS.Resource.InvalidFormat", HttpStatus.BAD_REQUEST,
            "Validation error", "Şema kontrolleri başarısız"),
    NOT_FOUND("TR.OHVPS.Resource.NotFound", HttpStatus.NOT_FOUND,
            "Resource not found", "Kaynak bulunamadı"),
    METHOD_NOT_ALLOWED("TR.OHVPS.Resource.MethodNotAllowed", HttpStatus.METHOD_NOT_ALLOWED,
            "Method not allowed", "İstek yapılan URL için izin verilmeyen metot"),
    NOT_ACCEPTABLE("TR.OHVPS.Resource.NotAcceptable", HttpStatus.NOT_ACCEPTABLE,
            "Not acceptable", "Kabul edilmedi"),
    UNSUPPORTED_MEDIA_TYPE("TR.OHVPS.Resource.UnsupportedMediaType", HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "Content type not supported", "Desteklenmeyen içerik tipi"),
    FORBIDDEN("TR.OHVPS.Resource.Forbidden", HttpStatus.FORBIDDEN,
            "Forbidden", "Erişim yetkisi bulunmamaktadır"),
    CONSENT_REVOKED("TR.OHVPS.Resource.ConsentRevoked", HttpStatus.FORBIDDEN,           // Bkz. not
            "Consent revoked or terminated", "Rıza iptal edilmiş veya sonlandırılmış"),
    CONSENT_MISMATCH("TR.OHVPS.Resource.ConsentMismatch", HttpStatus.FORBIDDEN,         // Bkz. not
            "Consent status not suitable", "Rıza durumu işleme uygun değil"),

    // ── Business: iş kuralı ihlalleri ──
    INCORRECT_PERMISSION_TYPE("TR.OHVPS.Business.IncorrectPermissionType", HttpStatus.BAD_REQUEST,
            "Incorrect permission type combination", "Hatalı izin türü kombinasyonu"),
    PERMISSION_TYPE_NOT_SUPPORTED("TR.OHVPS.Business.PermissionTypeNotSupported", HttpStatus.FORBIDDEN,   // Bkz. not
            "Permission not granted for this resource", "Bu kaynak için izin verilmemiş"),
    CONSENT_ALREADY_EXISTS("TR.OHVPS.Business.ConsentAlreadyExists", HttpStatus.BAD_REQUEST,
            "An active consent already exists", "Aktif bir rıza mevcut"),
    CONSENT_STATUS_NOT_FOR_UPDATE("TR.OHVPS.Business.ConsentStatusNotforUpdate", HttpStatus.BAD_REQUEST,
            "Consent status not suitable for update", "Rıza durumu güncellemeye uygun değil"),
    CUSTOMER_NOT_FOUND("TR.OHVPS.Business.CustomerNotFound", HttpStatus.BAD_REQUEST,
            "Customer not found", "Müşteri bulunamadı"),
    DECOUPLED_AUTHENTICATION_NOT_SUPPORTED("TR.OHVPS.Business.DecoupledAuthenticationNotSupported", HttpStatus.BAD_REQUEST,
            "Decoupled authentication not supported", "Ayrık GKD desteklenmiyor"),
    INVALID_ACCOUNT("TR.OHVPS.Business.InvalidAccount", HttpStatus.BAD_REQUEST,
            "Account is disabled or not found", "Hesap aktif değil veya bulunamadı"),

    // ── Connection: kimlik, token ve erişim ──
    INVALID_TOKEN("TR.OHVPS.Connection.InvalidToken", HttpStatus.UNAUTHORIZED,
            "Invalid or expired token", "Geçersiz veya süresi dolmuş belirteç"),
    INVALID_ASPSP("TR.OHVPS.Connection.InvalidASPSP", HttpStatus.BAD_REQUEST,
            "Invalid ASPSP code", "Geçersiz HHS kodu"),
    INVALID_TPP("TR.OHVPS.Connection.InvalidTPP", HttpStatus.BAD_REQUEST,
            "Invalid TPP code", "Geçersiz YÖS kodu"),
    INVALID_TPP_ROLE("TR.OHVPS.Connection.InvalidTPPRole", HttpStatus.FORBIDDEN,
            "Invalid TPP role", "Geçersiz YÖS rolü"),
    EXCEEDED_RATE("TR.OHVPS.Connection.ExceededRate", HttpStatus.TOO_MANY_REQUESTS,
            "Exceeded rate", "Erişim sıklığı limiti aşımı"),

    // ── Server ──
    INTERNAL_ERROR("TR.OHVPS.Server.InternalError", HttpStatus.INTERNAL_SERVER_ERROR,
            "Unexpected condition was encountered", "Beklenmeyen bir durumla karşılaşıldı"),
    SERVICE_UNAVAILABLE("TR.OHVPS.Server.ServiceUnavailable", HttpStatus.SERVICE_UNAVAILABLE,
            "Service unavailable", "Hizmet şu anda verilemiyor");

    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;
    private final String defaultMessageTr;

    OhvpsErrorCode(String code, HttpStatus status, String defaultMessage, String defaultMessageTr) {
        this.code = code;
        this.status = status;
        this.defaultMessage = defaultMessage;
        this.defaultMessageTr = defaultMessageTr;
    }

    public String code() { return code; }
    public HttpStatus status() { return status; }
    public String defaultMessage() { return defaultMessage; }
    public String defaultMessageTr() { return defaultMessageTr; }
}
