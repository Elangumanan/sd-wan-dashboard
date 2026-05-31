package com.example.sdwan.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

/**
 * Standardised error body returned for every non-2xx response.
 *
 * <pre>{@code
 * {
 *   "code":    "RESOURCE_NOT_FOUND",
 *   "message": "Organization not found: org-999",
 *   "details": [...]          // omitted when absent
 * }
 * }</pre>
 *
 * {@code details} is {@link Object} so it can hold a {@code List<String>}
 * for field-level validation errors or a structured map for richer payloads.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ErrorResponse {

    private final String code;
    private final String message;
    private final Object details;

    private ErrorResponse(String code, String message, Object details) {
        this.code    = Objects.requireNonNull(code,    "code");
        this.message = Objects.requireNonNull(message, "message");
        this.details = details;
    }

    /** Creates an error response without details. */
    public static ErrorResponse of(ErrorCode code, String message) {
        return new ErrorResponse(code.name(), message, null);
    }

    /** Creates an error response with an optional details payload. */
    public static ErrorResponse of(ErrorCode code, String message, Object details) {
        return new ErrorResponse(code.name(), message, details);
    }

    public String code()    { return code; }
    public String message() { return message; }
    public Object details() { return details; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ErrorResponse other)) return false;
        return Objects.equals(code, other.code)
            && Objects.equals(message, other.message)
            && Objects.equals(details, other.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, details);
    }

    @Override
    public String toString() {
        return "ErrorResponse[code=" + code + ", message=" + message + ", details=" + details + "]";
    }
}
