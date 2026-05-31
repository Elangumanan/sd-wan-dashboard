package com.example.sdwan.exception;

/**
 * Centralised factory for all application domain exceptions.
 *
 * <p>Services and other application code should call these methods instead of
 * constructing exception instances directly. This ensures:
 * <ul>
 *   <li>Message formats are consistent across the codebase.</li>
 *   <li>Error codes are always set correctly.</li>
 *   <li>A single place to adjust messages or add context in future.</li>
 * </ul>
 *
 * <p>This class is non-instantiable; all methods are static.
 */
public final class ExceptionFactory {

    private ExceptionFactory() {}

    // ── 404 ──────────────────────────────────────────────────────────────────

    /**
     * A named resource could not be found by the given identifier.
     *
     * @param resourceType human-readable type label, e.g. {@code "Organization"}
     * @param id           the identifier that was looked up
     */
    public static ResourceNotFoundException resourceNotFound(String resourceType, String id) {
        return new ResourceNotFoundException(resourceType, id);
    }

    // ── 400 ──────────────────────────────────────────────────────────────────

    /**
     * A field or parameter failed validation.
     *
     * @param message description of the constraint that was violated
     */
    public static ValidationException validationError(String message) {
        return new ValidationException(message);
    }

    /**
     * A specific named field failed validation.
     *
     * @param field   the field or parameter name
     * @param message the constraint that was violated
     */
    public static ValidationException validationError(String field, String message) {
        return new ValidationException(field, message);
    }

    // ── 422 ──────────────────────────────────────────────────────────────────

    /**
     * A domain or business rule prevented the operation from completing.
     * The request was syntactically valid but cannot be fulfilled given
     * the current state.
     *
     * @param message description of the rule that was violated
     */
    public static BusinessException businessError(String message) {
        return new BusinessException(message);
    }

    // ── 401 ──────────────────────────────────────────────────────────────────

    /**
     * The caller is not authenticated or the supplied credentials are invalid.
     */
    public static UnauthorizedException unauthorized() {
        return new UnauthorizedException("Access denied");
    }

    /**
     * The caller is not authenticated or the supplied credentials are invalid.
     *
     * @param message context-specific reason
     */
    public static UnauthorizedException unauthorized(String message) {
        return new UnauthorizedException(message);
    }
}
