package org.scoula.codef.common.exception;

public class CodefApiException extends RuntimeException {
    private final String errorCode;
    private final String errorMessage;

    public CodefApiException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}