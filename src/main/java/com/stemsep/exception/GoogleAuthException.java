package com.stemsep.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_GATEWAY)
public class GoogleAuthException extends AppException {
    public GoogleAuthException(String msg) { super(ErrorCode.GOOGLE_AUTH_FAILED, msg); }
}
