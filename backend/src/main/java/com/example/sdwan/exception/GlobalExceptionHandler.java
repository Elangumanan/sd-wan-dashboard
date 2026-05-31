package com.example.sdwan.exception;

import com.example.sdwan.response.ErrorCode;
import com.example.sdwan.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Centralised exception → HTTP response mapping.
 *
 * <p>Domain exceptions ({@link AppException} and subclasses) are handled by a
 * single method that reads the HTTP status directly from {@link AppException#httpStatus()}.
 * Spring MVC / infrastructure exceptions each have their own fixed-status handler.
 *
 * <p>4xx problems are logged at WARN; 5xx faults are logged at ERROR with stack trace.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── Domain exceptions (AppException hierarchy) ────────────────────────────
    //    One handler covers ResourceNotFoundException, ValidationException,
    //    BusinessException, UnauthorizedException, and any future AppException subclass.

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex,
                                                             HttpServletRequest request) {
        HttpStatus status = ex.httpStatus();
        if (status.is5xxServerError()) {
            log.error("Application exception [{}] ({}): {}", request.getRequestURI(), ex.errorCode(), ex.getMessage(), ex);
        } else {
            log.warn("Application exception [{}] ({}): {}", request.getRequestURI(), ex.errorCode(), ex.getMessage());
        }
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(ex.errorCode(), ex.getMessage()));
    }

    // ── Spring MVC infrastructure exceptions ──────────────────────────────────

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNoRouteFound(NoResourceFoundException ex,
                                             HttpServletRequest request) {
        String message = "No endpoint: " + request.getMethod() + " " + request.getRequestURI();
        log.warn("No route found: {}", message);
        return ErrorResponse.of(ErrorCode.ROUTE_NOT_FOUND, message);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorResponse handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex,
                                                 HttpServletRequest request) {
        String message = "HTTP method '" + ex.getMethod() + "' is not supported on this endpoint";
        log.warn("Method not allowed [{}]: {}", request.getRequestURI(), message);
        return ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex,
                                                HttpServletRequest request) {
        log.warn("Illegal argument [{}]: {}", request.getRequestURI(), ex.getMessage());
        return ErrorResponse.of(ErrorCode.ILLEGAL_ARGUMENT, ex.getMessage());
    }

    /** @Valid on a @RequestBody — returns per-field details. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                       HttpServletRequest request) {
        var details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        String summary = "Validation failed for " + details.size() + " field(s)";
        log.warn("Request body validation failed [{}]: {}", request.getRequestURI(), details);
        return ErrorResponse.of(ErrorCode.VALIDATION_ERROR, summary, details);
    }

    /** @Validated + @Pattern / @NotBlank on @RequestParam or @PathVariable. */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex,
                                                    HttpServletRequest request) {
        var details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();
        log.warn("Constraint violation [{}]: {}", request.getRequestURI(), details);
        return ErrorResponse.of(ErrorCode.CONSTRAINT_VIOLATION, "Constraint violation on request parameter", details);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                             HttpServletRequest request) {
        String message = "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'";
        log.warn("Type mismatch [{}]: {}", request.getRequestURI(), message);
        return ErrorResponse.of(ErrorCode.TYPE_MISMATCH, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingParameter(MissingServletRequestParameterException ex,
                                                 HttpServletRequest request) {
        String message = "Required parameter '" + ex.getParameterName() + "' ("
                + ex.getParameterType() + ") is missing";
        log.warn("Missing parameter [{}]: {}", request.getRequestURI(), message);
        return ErrorResponse.of(ErrorCode.MISSING_PARAMETER, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMessageNotReadable(HttpMessageNotReadableException ex,
                                                   HttpServletRequest request) {
        log.warn("Unreadable request body [{}]: {}", request.getRequestURI(), ex.getMessage());
        return ErrorResponse.of(ErrorCode.MALFORMED_REQUEST, "Request body is missing or malformed");
    }

    // ── Safety nets ───────────────────────────────────────────────────────────

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        log.error("Unhandled runtime exception [{}]", request.getRequestURI(), ex);
        return ErrorResponse.of(ErrorCode.INTERNAL_ERROR, "An unexpected error occurred");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneral(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception [{}]", request.getRequestURI(), ex);
        return ErrorResponse.of(ErrorCode.INTERNAL_ERROR, "An unexpected error occurred");
    }
}
