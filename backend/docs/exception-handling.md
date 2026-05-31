# Exception Handling

## Design Goals

1. **Consistent error shape** — every non-2xx response has the same JSON structure.
2. **Application-level error codes** — consumers get `"RESOURCE_NOT_FOUND"` not `404`, enabling programmatic handling independent of HTTP status.
3. **Single handler for domain exceptions** — adding a new domain exception requires no changes to `GlobalExceptionHandler`.
4. **Centralised creation** — `ExceptionFactory` is the only place in `src/main` that instantiates domain exception objects.

---

## Exception Hierarchy

```
java.lang.RuntimeException
  └── AppException  (abstract)
        ├── ResourceNotFoundException   RESOURCE_NOT_FOUND → HTTP 404
        ├── ValidationException         VALIDATION_ERROR   → HTTP 400
        ├── BusinessException           BUSINESS_ERROR     → HTTP 422
        └── UnauthorizedException       UNAUTHORIZED       → HTTP 401
```

### `AppException` — the base contract

```java
public abstract class AppException extends RuntimeException {
    private final ErrorCode errorCode;

    public ErrorCode errorCode()  { return errorCode; }
    public HttpStatus httpStatus(){ return errorCode.httpStatus(); }
}
```

Every subclass passes an `ErrorCode` to `AppException`. The error code owns its own HTTP status — `GlobalExceptionHandler` never hard-codes status values for domain exceptions.

---

## `ErrorCode` — Single Source of Truth for HTTP Status

```java
public enum ErrorCode {
    RESOURCE_NOT_FOUND  (HttpStatus.NOT_FOUND),
    ROUTE_NOT_FOUND     (HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED  (HttpStatus.METHOD_NOT_ALLOWED),
    UNAUTHORIZED        (HttpStatus.UNAUTHORIZED),
    VALIDATION_ERROR    (HttpStatus.BAD_REQUEST),
    CONSTRAINT_VIOLATION(HttpStatus.BAD_REQUEST),
    ILLEGAL_ARGUMENT    (HttpStatus.BAD_REQUEST),
    TYPE_MISMATCH       (HttpStatus.BAD_REQUEST),
    MISSING_PARAMETER   (HttpStatus.BAD_REQUEST),
    MALFORMED_REQUEST   (HttpStatus.BAD_REQUEST),
    BUSINESS_ERROR      (HttpStatus.UNPROCESSABLE_ENTITY),
    INTERNAL_ERROR      (HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus httpStatus;
    public HttpStatus httpStatus() { return httpStatus; }
}
```

Changing the HTTP status for any error code requires editing exactly one line.

---

## `ExceptionFactory` — Factory Method Pattern

All service code calls `ExceptionFactory` instead of constructing exceptions directly. This guarantees:
- Message format is consistent across the whole codebase
- `ErrorCode` is always set correctly
- Future changes (e.g. adding context, switching message locale) happen in one place

```java
// Services use:
ExceptionFactory.resourceNotFound("Organization", id)
ExceptionFactory.validationError("name", "must not be blank")
ExceptionFactory.businessError("Site is not active")
ExceptionFactory.unauthorized()
ExceptionFactory.unauthorized("Invalid API key")

// NOT:
new ResourceNotFoundException("Organization", id)   // ← never in service code
```

`ExceptionFactory` is `final` with a private constructor — it cannot be instantiated or subclassed.

**Verification:** `ExceptionFactory.java` is the **only** file in `src/main` that contains `new ResourceNotFoundException(...)` or `new ValidationException(...)`. A grep confirms this after any refactor.

### Factory Methods

| Method | Returns | Code | HTTP |
|---|---|---|---|
| `resourceNotFound(type, id)` | `ResourceNotFoundException` | `RESOURCE_NOT_FOUND` | 404 |
| `validationError(message)` | `ValidationException` | `VALIDATION_ERROR` | 400 |
| `validationError(field, message)` | `ValidationException` | `VALIDATION_ERROR` | 400 |
| `businessError(message)` | `BusinessException` | `BUSINESS_ERROR` | 422 |
| `unauthorized()` | `UnauthorizedException` | `UNAUTHORIZED` | 401 |
| `unauthorized(message)` | `UnauthorizedException` | `UNAUTHORIZED` | 401 |

---

## `GlobalExceptionHandler` — Handler Catalogue

Annotated with `@RestControllerAdvice`. Handlers run in specificity order (most specific wins).

### Domain exceptions — one handler for all

```java
@ExceptionHandler(AppException.class)
public ResponseEntity<ErrorResponse> handleAppException(AppException ex, ...) {
    HttpStatus status = ex.httpStatus();          // ← read from ErrorCode
    return ResponseEntity.status(status)
        .body(ErrorResponse.of(ex.errorCode(), ex.getMessage()));
}
```

Because every domain exception is an `AppException`, this single handler covers `ResourceNotFoundException`, `ValidationException`, `BusinessException`, `UnauthorizedException`, and any future subclass.

### Spring MVC / infrastructure exceptions

| Exception | Status | Code |
|---|---|---|
| `NoResourceFoundException` | 404 | `ROUTE_NOT_FOUND` |
| `HttpRequestMethodNotSupportedException` | 405 | `METHOD_NOT_ALLOWED` |
| `IllegalArgumentException` | 400 | `ILLEGAL_ARGUMENT` |
| `MethodArgumentNotValidException` | 400 | `VALIDATION_ERROR` + `details[]` |
| `ConstraintViolationException` | 400 | `CONSTRAINT_VIOLATION` + `details[]` |
| `MethodArgumentTypeMismatchException` | 400 | `TYPE_MISMATCH` |
| `MissingServletRequestParameterException` | 400 | `MISSING_PARAMETER` |
| `HttpMessageNotReadableException` | 400 | `MALFORMED_REQUEST` |
| `RuntimeException` | 500 | `INTERNAL_ERROR` |
| `Exception` | 500 | `INTERNAL_ERROR` |

### Logging strategy

| Status range | Log level | Stack trace |
|---|---|---|
| 4xx | `WARN` | No |
| 5xx | `ERROR` | Yes (full stack trace via `log.error(..., ex)`) |

---

## `ErrorResponse` — Wire Format

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ErrorResponse {
    private final String code;      // Required — always present
    private final String message;   // Required — always present
    private final Object details;   // Optional — omitted from JSON when null
}
```

`details` is `Object` (not `List<String>`) to support both flat validation lists and structured payloads.

### Example — simple error
```json
{
  "code": "RESOURCE_NOT_FOUND",
  "message": "Organization not found: org-999"
}
```

### Example — validation error with details
```json
{
  "code": "VALIDATION_ERROR",
  "message": "Validation failed for 2 field(s)",
  "details": [
    "name: must not be blank",
    "region: size must be between 2 and 50"
  ]
}
```

---

## Ownership Chain Validation

For nested path requests (`/organizations/{orgId}/sites/{siteId}/devices/{deviceId}`), `DeviceServiceImpl.resolveDevice()` validates the full hierarchy before returning data:

```
1. Check org-001 exists → throw RESOURCE_NOT_FOUND("Organization") if not
2. Check site-001 exists AND site.orgId == "org-001" → throw RESOURCE_NOT_FOUND("Site") if not
3. Check dev-001 exists AND device.siteId == "site-001" → throw RESOURCE_NOT_FOUND("Device") if not
```

This prevents information leakage — a request for `GET /org-999/sites/site-001` returns `Organization not found: org-999`, not `Site not found: site-001`.

---

## Adding a New Exception Type

1. Create `MyNewException extends AppException` — pass the appropriate `ErrorCode` to `super()`.
2. Add a factory method to `ExceptionFactory`.
3. If the error needs a new `ErrorCode`, add one to the enum with its HTTP status.
4. No changes to `GlobalExceptionHandler` required.
