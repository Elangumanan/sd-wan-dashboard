package com.example.sdwan.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

/**
 * Standardised success body wrapping every 2xx response payload.
 *
 * <pre>{@code
 * {
 *   "value":   { ... },          // required — the actual response data
 *   "message": "Created"         // omitted when absent
 * }
 * }</pre>
 *
 * @param <T> type of the response payload
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class SuccessResponse<T> {

    private final T      value;
    private final String message;

    private SuccessResponse(T value, String message) {
        this.value   = Objects.requireNonNull(value, "value");
        this.message = message;
    }

    /** Wraps a payload with no accompanying message. */
    public static <T> SuccessResponse<T> of(T value) {
        return new SuccessResponse<>(value, null);
    }

    /** Wraps a payload with an optional human-readable message. */
    public static <T> SuccessResponse<T> of(T value, String message) {
        return new SuccessResponse<>(value, message);
    }

    public T      value()   { return value; }
    public String message() { return message; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SuccessResponse<?> other)) return false;
        return Objects.equals(value, other.value)
            && Objects.equals(message, other.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, message);
    }

    @Override
    public String toString() {
        return "SuccessResponse[value=" + value + ", message=" + message + "]";
    }
}
