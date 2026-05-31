package com.example.sdwan.exception;

import com.example.sdwan.response.ErrorCode;

/**
 * Thrown when the caller lacks the required identity or permissions.
 * Maps to HTTP 401 Unauthorized.
 */
public class UnauthorizedException extends AppException {

    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }
}
