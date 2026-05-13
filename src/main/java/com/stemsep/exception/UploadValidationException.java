package com.stemsep.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Yükleme validasyonu başarısız (boş dosya, çok büyük, geçersiz format).
 * Hangi alt-koşulun ihlal edildiğini {@link ErrorCode} sabiti taşır
 * (UPLOAD_EMPTY / UPLOAD_TOO_LARGE / UPLOAD_INVALID_FORMAT).
 *
 * <p>Mevcut {@code UploadController.handleUpload} flash attribute ile
 * kendi flow'unu yönetiyor; bu exception slayt-uyumlu opt-in alternatif
 * (örn. Service katmanından fırlatılabilir).</p>
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class UploadValidationException extends AppException {
    public UploadValidationException(ErrorCode code, String msg) {
        super(code, msg);
        if (code != ErrorCode.UPLOAD_EMPTY
                && code != ErrorCode.UPLOAD_TOO_LARGE
                && code != ErrorCode.UPLOAD_INVALID_FORMAT
                && code != ErrorCode.UPLOAD_GENERIC) {
            throw new IllegalArgumentException("UploadValidationException requires UPLOAD_* ErrorCode, got: " + code);
        }
    }
}
