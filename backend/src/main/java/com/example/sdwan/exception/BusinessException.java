package com.example.sdwan.exception;

import com.example.sdwan.response.ErrorCode;

/**
 * Thrown when an operation is rejected due to a domain or business rule violation.
 * Maps to HTTP 422 Unprocessable Entity — the request was valid but cannot be
 * fulfilled given the current state of the resource.
 */
public class BusinessException extends AppException {

    public BusinessException(String message) {
        super(ErrorCode.BUSINESS_ERROR, message);
    }
}
