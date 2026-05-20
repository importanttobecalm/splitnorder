package com.stemsep.exception;

import org.springframework.http.HttpStatus;

/**
 * Uygulama genelinde tüm hata kodlarının merkezi tek kaynağı.
 *
 * <p><b>Slayt referansı:</b> BM470 "Çalışma Zamanı Exception'ı ile Durum Ayarı"
 * (proje slaytı tr.edu.duzce.mf.bm.bm470.exception paketi). Slaytta her
 * exception sınıfı kendi {@code @ResponseStatus} annotation'ı ile HTTP
 * koduna bağlanır; bu enum, mevcut o pattern'i BOZMAZ — sadece
 * {@code (code → http status, i18n key)} eşlemesini tek dosyada konsolide
 * eder. Her custom exception sınıfı {@link #getCode()} ile bu enum sabitine
 * referans verir.</p>
 *
 * <p><b>i18n contract:</b> {@code I18nKeyContractTest} bu enum üzerinden
 * reflection ile gezer ve her {@link #messageKey} TR+EN properties
 * dosyalarında var mı diye otomatik doğrular. Yeni hata eklemek için
 * tek yapılması gereken: bu enum'a sabit + TR/EN'e iki satır key.</p>
 *
 * <p><b>Slayt-dışı uyarı:</b> {@code @ControllerAdvice} / global
 * {@code @ExceptionHandler} BM470 slaytlarında HİÇ geçmiyor — bilinçli
 * olarak kullanılmadı. Hata yönetimi 100% slayt-uyumlu:
 * {@code throw new XException()} → Spring otomatik {@code @ResponseStatus}
 * okur → ilgili HTTP cevabı.</p>
 */
public enum ErrorCode {

    // ===== Auth — kullanıcı kimlik & doğrulama =====
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "auth.error.INVALID_CREDENTIALS"),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "auth.error.EMAIL_NOT_VERIFIED"),
    USERNAME_EXISTS(HttpStatus.CONFLICT, "auth.error.USERNAME_EXISTS"),
    EMAIL_EXISTS(HttpStatus.CONFLICT, "auth.error.EMAIL_EXISTS"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "auth.error.USER_NOT_FOUND"),
    USE_GOOGLE_LOGIN(HttpStatus.CONFLICT, "auth.error.USE_GOOGLE_LOGIN"),
    GOOGLE_AUTH_FAILED(HttpStatus.BAD_GATEWAY, "auth.error.GOOGLE_AUTH_FAILED"),
    ALREADY_VERIFIED(HttpStatus.CONFLICT, "auth.error.ALREADY_VERIFIED"),

    // ===== Auth — token akışları (e-posta doğrulama + parola sıfırlama) =====
    INVALID_TOKEN(HttpStatus.NOT_FOUND, "auth.error.INVALID_TOKEN"),
    TOKEN_EXPIRED(HttpStatus.GONE, "auth.error.TOKEN_EXPIRED"),
    INVALID_OR_EXPIRED_TOKEN(HttpStatus.NOT_FOUND, "auth.error.INVALID_OR_EXPIRED_TOKEN"),

    // ===== Auth — form validasyon =====
    USERNAME_REQUIRED(HttpStatus.BAD_REQUEST, "auth.error.USERNAME_REQUIRED"),
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, "auth.error.INVALID_EMAIL"),
    PASSWORD_TOO_SHORT(HttpStatus.BAD_REQUEST, "auth.error.PASSWORD_TOO_SHORT"),
    PASSWORD_WEAK(HttpStatus.BAD_REQUEST, "auth.error.PASSWORD_WEAK"),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "auth.error.PASSWORD_MISMATCH"),

    // ===== Job & inference =====
    JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "job.error.NOT_FOUND"),
    UNAUTHORIZED_JOB_ACCESS(HttpStatus.FORBIDDEN, "job.error.UNAUTHORIZED"),
    INFERENCE_FAILED(HttpStatus.BAD_GATEWAY, "job.error.INFERENCE_FAILED"),

    // ===== Upload =====
    UPLOAD_EMPTY(HttpStatus.BAD_REQUEST, "upload.error.empty"),
    UPLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "upload.error.tooLarge"),
    UPLOAD_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "upload.error.invalidFormat"),
    UPLOAD_GENERIC(HttpStatus.INTERNAL_SERVER_ERROR, "upload.error.generic"),

    // ===== Storage / Kota =====
    STORAGE_QUOTA_EXCEEDED(HttpStatus.INSUFFICIENT_STORAGE, "storage.error.quotaExceeded"),

    // ===== Generic / yakalanamayan =====
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "auth.error.INTERNAL_ERROR"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "error.404.title");

    private final HttpStatus status;
    private final String messageKey;

    ErrorCode(HttpStatus status, String messageKey) {
        this.status = status;
        this.messageKey = messageKey;
    }

    public HttpStatus getStatus() {
        return status;
    }

    /**
     * messages_*.properties dosyalarındaki tam i18n key (örn.
     * "auth.error.INVALID_CREDENTIALS"). I18nKeyContractTest bunu doğrular.
     */
    public String getMessageKey() {
        return messageKey;
    }
}
