package com.stemsep.exception;

public class VerificationTokenExpiredException extends RuntimeException {
    private final String email;

    public VerificationTokenExpiredException(String email) {
        super("Verification token expired for email=" + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
