package com.stemsep.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class UsernameExistsException extends AppException {
    public UsernameExistsException(String msg) { super(ErrorCode.USERNAME_EXISTS, msg); }
}
