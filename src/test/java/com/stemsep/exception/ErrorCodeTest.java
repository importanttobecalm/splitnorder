package com.stemsep.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ErrorCode} enum'unun davranışsal birim testleri.
 *
 * <p>Bu testler enum'un sözleşmesini doğrular: her sabit non-null HttpStatus
 * ve non-blank messageKey taşımalı, hiçbir sabit yanlışlıkla aynı i18n
 * key'ini paylaşmamalı, ve her custom exception'ın hata kodu doğru sabite
 * eşleşmeli.</p>
 *
 * <p><b>Tasarım amacı:</b> ErrorCode enum'u tüm hata kodlarının tek
 * source-of-truth'u — Controller'lar, Service'ler ve i18n properties
 * dosyaları hep buna referans verir. Bu test set'i o kontratın bozulmasını
 * önler.</p>
 */
public class ErrorCodeTest {

    /**
     * Her enum sabiti {@code HttpStatus} taşımalı (Spring'in HTTP cevabı
     * için gerekli). Null bir status üretim runtime'da NPE'ye dönüşür.
     */
    @Test
    public void everyCode_hasNonNullStatus() {
        for (ErrorCode code : ErrorCode.values()) {
            assertNotNull(code.getStatus(), "ErrorCode." + code.name() + " missing status");
        }
    }

    /**
     * Her enum sabiti boş olmayan messageKey taşımalı — i18n lookup'ı için
     * zorunlu. I18nKeyContractTest bu key'lerin properties'te var olduğunu
     * ayrıca doğrular.
     */
    @Test
    public void everyCode_hasNonBlankMessageKey() {
        for (ErrorCode code : ErrorCode.values()) {
            String key = code.getMessageKey();
            assertNotNull(key, "ErrorCode." + code.name() + " null key");
            assertFalse(key.isBlank(), "ErrorCode." + code.name() + " blank key");
        }
    }

    /**
     * Auth hata kodları belirli HTTP semantiğine bağlı:
     * INVALID_CREDENTIALS = 401 (kimlik yanlış),
     * EMAIL_NOT_VERIFIED = 403 (kimlik doğru ama yetki yok henüz),
     * EMAIL_EXISTS/USERNAME_EXISTS = 409 (kayıt çakışması).
     * Bu eşlemeler değişirse istemciler kırılır.
     */
    @Test
    public void authCodes_haveCorrectHttpStatus() {
        assertEquals(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_CREDENTIALS.getStatus());
        assertEquals(HttpStatus.FORBIDDEN, ErrorCode.EMAIL_NOT_VERIFIED.getStatus());
        assertEquals(HttpStatus.CONFLICT, ErrorCode.EMAIL_EXISTS.getStatus());
        assertEquals(HttpStatus.CONFLICT, ErrorCode.USERNAME_EXISTS.getStatus());
        assertEquals(HttpStatus.GONE, ErrorCode.TOKEN_EXPIRED.getStatus());
        assertEquals(HttpStatus.NOT_FOUND, ErrorCode.INVALID_TOKEN.getStatus());
    }

    /**
     * Job hata kodları: 404 = bulunamadı, 403 = başkasına ait,
     * 502 = upstream Demucs servisi cevap vermedi.
     */
    @Test
    public void jobCodes_haveCorrectHttpStatus() {
        assertEquals(HttpStatus.NOT_FOUND, ErrorCode.JOB_NOT_FOUND.getStatus());
        assertEquals(HttpStatus.FORBIDDEN, ErrorCode.UNAUTHORIZED_JOB_ACCESS.getStatus());
        assertEquals(HttpStatus.BAD_GATEWAY, ErrorCode.INFERENCE_FAILED.getStatus());
    }

    /**
     * Exception ↔ ErrorCode eşlemesi: her custom exception fırlatıldığında
     * {@link AppException#getCode()} doğru sabite resolve etmeli.
     * Refactor sırasında bağlantı kopabilir; bu test koruma sağlar.
     */
    @Test
    public void exceptionsMapToCorrectErrorCode() {
        assertEquals(ErrorCode.INVALID_CREDENTIALS,
                new InvalidCredentialsException("x").getCode());
        assertEquals(ErrorCode.EMAIL_NOT_VERIFIED,
                new EmailNotVerifiedException("x").getCode());
        assertEquals(ErrorCode.EMAIL_EXISTS,
                new EmailExistsException("x").getCode());
        assertEquals(ErrorCode.USERNAME_EXISTS,
                new UsernameExistsException("x").getCode());
        assertEquals(ErrorCode.USER_NOT_FOUND,
                new UserNotFoundException("x").getCode());
        assertEquals(ErrorCode.INVALID_TOKEN,
                new InvalidTokenException("x").getCode());
        assertEquals(ErrorCode.TOKEN_EXPIRED,
                new VerificationTokenExpiredException("a@b.com").getCode());
        assertEquals(ErrorCode.GOOGLE_AUTH_FAILED,
                new GoogleAuthException("x").getCode());
        assertEquals(ErrorCode.INFERENCE_FAILED,
                new InferenceFailedException("x").getCode());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND,
                new ResourceNotFoundException().getCode());
        assertEquals(ErrorCode.JOB_NOT_FOUND,
                new JobNotFoundException("uuid").getCode());
        assertEquals(ErrorCode.UNAUTHORIZED_JOB_ACCESS,
                new UnauthorizedJobAccessException(1L, "uuid").getCode());
    }

    /**
     * UploadValidationException SADECE UPLOAD_* kodlarını kabul etmeli;
     * yanlış kategori (örn. INVALID_CREDENTIALS) verilirse constructor
     * IllegalArgumentException fırlatır. Bu, copy-paste regression
     * koruması.
     */
    @Test
    public void uploadException_rejectsNonUploadErrorCodes() {
        // Valid UPLOAD_* kabul edilmeli
        assertDoesNotThrow(() ->
                new UploadValidationException(ErrorCode.UPLOAD_EMPTY, "boş"));
        assertDoesNotThrow(() ->
                new UploadValidationException(ErrorCode.UPLOAD_TOO_LARGE, "büyük"));

        // Non-UPLOAD reddedilmeli
        assertThrows(IllegalArgumentException.class, () ->
                new UploadValidationException(ErrorCode.INVALID_CREDENTIALS, "yanlış"));
        assertThrows(IllegalArgumentException.class, () ->
                new UploadValidationException(ErrorCode.JOB_NOT_FOUND, "yanlış"));
    }

    /**
     * VerificationTokenExpiredException email bilgisini taşımalı —
     * Controller bunu kullanıp login redirect'inde query param olarak
     * geçirir ki kullanıcı resend formunu doldurulmuş halde görsün.
     */
    @Test
    public void tokenExpiredException_carriesEmail() {
        VerificationTokenExpiredException ex = new VerificationTokenExpiredException("u@b.com");
        assertEquals("u@b.com", ex.getEmail());
    }
}
