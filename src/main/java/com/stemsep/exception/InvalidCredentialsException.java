package com.stemsep.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class InvalidCredentialsException extends AppException {
    public InvalidCredentialsException(String msg) { super(ErrorCode.INVALID_CREDENTIALS, msg); }
}
