package com.example.sdwan.exception;

import com.example.sdwan.response.ErrorCode;

public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String resourceType, String id) {
        super(ErrorCode.RESOURCE_NOT_FOUND, resourceType + " not found: " + id);
    }
}
