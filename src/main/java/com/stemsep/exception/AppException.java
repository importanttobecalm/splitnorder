package com.stemsep.exception;

/**
 * Uygulama hatalarının ortak temel sınıfı. Tüm custom exception sınıfları
 * bunu extend eder ve kendi {@link ErrorCode} sabitlerini taşırlar.
 *
 * <p><b>Slayt uyumu:</b> Slayt pattern'inde her exception kendi
 * {@code @ResponseStatus} annotation'ı taşır ve {@code RuntimeException}
 * extend eder. Bu sınıf o kuralı bozmaz — sadece {@link ErrorCode} alanı
 * ekleyerek hata kodlarını tek source-of-truth olarak yönetmemizi sağlar.
 * Mevcut constructor/davranış aynen korunur; {@code @ResponseStatus}
 * yine her concrete alt sınıfta tanımlıdır (Spring bunu okur).</p>
 *
 * <p><b>Neden RuntimeException?</b> Slayt birebir; Spring MVC controller
 * imzalarına {@code throws} eklemek zorunda kalmamak için unchecked.</p>
 */
public abstract class AppException extends RuntimeException {

    private final ErrorCode code;

    protected AppException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    protected AppException(ErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }
}
