package com.stemsep.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_GATEWAY)
public class InferenceFailedException extends AppException {
    public InferenceFailedException(String msg) { super(ErrorCode.INFERENCE_FAILED, msg); }
}
