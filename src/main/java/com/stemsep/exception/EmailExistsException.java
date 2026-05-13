package com.stemsep.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class EmailExistsException extends AppException {
    public EmailExistsException(String msg) { super(ErrorCode.EMAIL_EXISTS, msg); }
}
