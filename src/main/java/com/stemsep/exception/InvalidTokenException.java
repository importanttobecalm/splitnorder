package com.stemsep.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class InvalidTokenException extends AppException {
    public InvalidTokenException(String msg) { super(ErrorCode.INVALID_TOKEN, msg); }
}
