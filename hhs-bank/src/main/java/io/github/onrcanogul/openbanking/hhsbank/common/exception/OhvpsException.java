package io.github.onrcanogul.openbanking.hhsbank.common.exception;

public class OhvpsException extends RuntimeException {

    private final OhvpsErrorCode errorCode;

    public OhvpsException(OhvpsErrorCode errorCode, String internalMessage) {
        super(internalMessage);
        this.errorCode = errorCode;
    }

    public OhvpsException(OhvpsErrorCode errorCode, String internalMessage, Throwable cause) {
        super(internalMessage, cause);
        this.errorCode = errorCode;
    }

    public OhvpsErrorCode errorCode() { return errorCode; }
}