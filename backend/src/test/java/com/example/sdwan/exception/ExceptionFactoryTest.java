package com.example.sdwan.exception;

import com.example.sdwan.response.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionFactoryTest {

    // ── resourceNotFound ──────────────────────────────────────────────────────

    @Test
    void resourceNotFound_returnsCorrectTypeAndCode() {
        var ex = ExceptionFactory.resourceNotFound("Organization", "org-999");

        assertThat(ex).isInstanceOf(ResourceNotFoundException.class);
        assertThat(ex.errorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        assertThat(ex.httpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).isEqualTo("Organization not found: org-999");
    }

    // ── validationError ───────────────────────────────────────────────────────

    @Test
    void validationError_withMessage_returnsCorrectTypeAndCode() {
        var ex = ExceptionFactory.validationError("name must not be blank");

        assertThat(ex).isInstanceOf(ValidationException.class);
        assertThat(ex.errorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
        assertThat(ex.httpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getMessage()).isEqualTo("name must not be blank");
    }

    @Test
    void validationError_withFieldAndMessage_formatsMessage() {
        var ex = ExceptionFactory.validationError("region", "must not be blank");

        assertThat(ex).isInstanceOf(ValidationException.class);
        assertThat(ex.errorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
        assertThat(ex.getMessage()).isEqualTo("'region': must not be blank");
    }

    // ── businessError ─────────────────────────────────────────────────────────

    @Test
    void businessError_returnsCorrectTypeAndCode() {
        var ex = ExceptionFactory.businessError("Site is not active");

        assertThat(ex).isInstanceOf(BusinessException.class);
        assertThat(ex.errorCode()).isEqualTo(ErrorCode.BUSINESS_ERROR);
        assertThat(ex.httpStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(ex.getMessage()).isEqualTo("Site is not active");
    }

    // ── unauthorized ──────────────────────────────────────────────────────────

    @Test
    void unauthorized_noArgs_usesDefaultMessage() {
        var ex = ExceptionFactory.unauthorized();

        assertThat(ex).isInstanceOf(UnauthorizedException.class);
        assertThat(ex.errorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
        assertThat(ex.httpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ex.getMessage()).isEqualTo("Access denied");
    }

    @Test
    void unauthorized_withMessage_usesProvidedMessage() {
        var ex = ExceptionFactory.unauthorized("Invalid API key");

        assertThat(ex).isInstanceOf(UnauthorizedException.class);
        assertThat(ex.getMessage()).isEqualTo("Invalid API key");
    }

    // ── AppException contract ─────────────────────────────────────────────────

    @Test
    void allExceptions_extendAppException() {
        assertThat(ExceptionFactory.resourceNotFound("X", "1")).isInstanceOf(AppException.class);
        assertThat(ExceptionFactory.validationError("bad")).isInstanceOf(AppException.class);
        assertThat(ExceptionFactory.businessError("rule")).isInstanceOf(AppException.class);
        assertThat(ExceptionFactory.unauthorized()).isInstanceOf(AppException.class);
    }

    @Test
    void errorCode_httpStatus_isConsistent() {
        assertThat(ErrorCode.RESOURCE_NOT_FOUND.httpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ErrorCode.VALIDATION_ERROR.httpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ErrorCode.BUSINESS_ERROR.httpStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(ErrorCode.UNAUTHORIZED.httpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ErrorCode.INTERNAL_ERROR.httpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
