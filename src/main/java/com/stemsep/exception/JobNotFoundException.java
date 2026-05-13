package com.stemsep.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Belirtilen publicId'ye sahip Job veritabanında yoksa fırlatılır.
 * Slayt birebir pattern: {@code @ResponseStatus(NOT_FOUND)} + custom
 * RuntimeException (proje slaytı "Çalışma Zamanı Exception'ı ile Durum
 * Ayarı").
 *
 * <p>JobController'da yumuşak akış için bazı yerlerde {@code redirect:/history}
 * tercih edilir (UX), bu exception ise JSON/AJAX endpoint'lerinde ve
 * doğrudan 404 dönmesi istenen yerlerde kullanılır.</p>
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class JobNotFoundException extends AppException {
    public JobNotFoundException(String publicId) {
        super(ErrorCode.JOB_NOT_FOUND, "Job not found: publicId=" + publicId);
    }
}
