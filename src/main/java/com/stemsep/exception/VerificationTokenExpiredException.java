package com.stemsep.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.GONE)
public class VerificationTokenExpiredException extends AppException {
    private final String email;

    public VerificationTokenExpiredException(String email) {
        super(ErrorCode.TOKEN_EXPIRED, "Verification token expired for email=" + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
