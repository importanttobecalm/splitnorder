package com.stemsep.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class EmailNotVerifiedException extends AppException {
    public EmailNotVerifiedException(String msg) { super(ErrorCode.EMAIL_NOT_VERIFIED, msg); }
}
