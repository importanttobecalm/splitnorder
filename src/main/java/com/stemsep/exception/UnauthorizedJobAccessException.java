package com.stemsep.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Bir kullanıcı kendisine ait olmayan bir Job'a erişmeye çalışırsa fırlatılır.
 * Mevcut JobController metodları çoğunlukla {@code response.sendError(403)}
 * kullanır; bu exception, slayt-uyumlu alternatifi sunar — Service katmanından
 * fırlatılabilir, Spring otomatik 403 cevabı döner.
 */
@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class UnauthorizedJobAccessException extends AppException {
    public UnauthorizedJobAccessException(Long userId, String publicId) {
        super(ErrorCode.UNAUTHORIZED_JOB_ACCESS,
                "User " + userId + " attempted to access job " + publicId);
    }
}
