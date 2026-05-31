package com.example.sdwan.exception;

import com.example.sdwan.response.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Objects;

/**
 * Base class for all application domain exceptions.
 * Carries an {@link ErrorCode} which owns the HTTP status, so the
 * GlobalExceptionHandler can handle every domain exception in a single method.
 */
public abstract class AppException extends RuntimeException {

    private final ErrorCode errorCode;

    protected AppException(ErrorCode errorCode, String message) {
        super(Objects.requireNonNull(message, "message"));
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode");
    }

    protected AppException(ErrorCode errorCode, String message, Throwable cause) {
        super(Objects.requireNonNull(message, "message"), cause);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode");
    }

    public ErrorCode errorCode() {
        return errorCode;
    }

    public HttpStatus httpStatus() {
        return errorCode.httpStatus();
    }
}
