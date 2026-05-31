package com.example.sdwan.exception;

import com.example.sdwan.response.ErrorCode;

public class ValidationException extends AppException {

    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
    }

    public ValidationException(String field, String message) {
        super(ErrorCode.VALIDATION_ERROR, "'" + field + "': " + message);
    }
}
