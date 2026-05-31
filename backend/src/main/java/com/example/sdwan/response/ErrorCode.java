package com.example.sdwan.response;

import org.springframework.http.HttpStatus;

/**
 * Application-level error codes.
 * Each code owns its HTTP status — GlobalExceptionHandler reads it from here,
 * keeping the status mapping in one place rather than scattered across handlers.
 */
public enum ErrorCode {

    // ── 404 ──────────────────────────────────────────────────────────────────
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND),
    ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND),

    // ── 405 ──────────────────────────────────────────────────────────────────
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED),

    // ── 401 ──────────────────────────────────────────────────────────────────
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),

    // ── 400 ──────────────────────────────────────────────────────────────────
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
    CONSTRAINT_VIOLATION(HttpStatus.BAD_REQUEST),
    ILLEGAL_ARGUMENT(HttpStatus.BAD_REQUEST),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST),

    // ── 422 ──────────────────────────────────────────────────────────────────
    BUSINESS_ERROR(HttpStatus.UNPROCESSABLE_ENTITY),

    // ── 500 ──────────────────────────────────────────────────────────────────
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus httpStatus;

    ErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }
}
