package com.stemsep.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Kullanıcının 5 GB'lık depo kotası dolu — yeni upload veya mix üretimi
 * reddedildi. HTTP 507 Insufficient Storage döner.
 */
@ResponseStatus(value = HttpStatus.INSUFFICIENT_STORAGE)
public class StorageQuotaExceededException extends AppException {
    public StorageQuotaExceededException(String msg) {
        super(ErrorCode.STORAGE_QUOTA_EXCEEDED, msg);
    }
}
