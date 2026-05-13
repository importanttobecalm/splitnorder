package com.stemsep.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class UserNotFoundException extends AppException {
    public UserNotFoundException(String msg) { super(ErrorCode.USER_NOT_FOUND, msg); }
}
