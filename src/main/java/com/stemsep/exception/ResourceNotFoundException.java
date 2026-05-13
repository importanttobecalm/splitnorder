package com.stemsep.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends AppException {
    public ResourceNotFoundException() {
        super(ErrorCode.RESOURCE_NOT_FOUND, "Kaynak bulunamadı!");
    }
    public ResourceNotFoundException(String msg) {
        super(ErrorCode.RESOURCE_NOT_FOUND, msg);
    }
}
