package com.stemsep.service;

import com.stemsep.dao.UserDao;
import com.stemsep.exception.EmailExistsException;
import com.stemsep.exception.EmailNotVerifiedException;
import com.stemsep.exception.InvalidCredentialsException;
import com.stemsep.exception.InvalidTokenException;
import com.stemsep.exception.UserNotFoundException;
import com.stemsep.exception.UsernameExistsException;
import com.stemsep.exception.VerificationTokenExpiredException;
import com.stemsep.model.User;
import java.time.LocalDateTime;
import com.stemsep.util.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link AuthService} için izole birim testleri.
 *
 * <p>Bu testler gerçek veritabanı + gerçek email gönderim'i devre dışı bırakır:
 * {@link UserDao} ve {@link EmailService} Mockito ile mock'lanır, böylece
 * Service'in iş kurallarını (doğru exception tipi, doğru state geçişi)
 * yalıtılmış şekilde doğrulayabiliriz.</p>
 *
 * <p><b>Slayt referansı:</b> "Service Sınıfları (@Service + @Transactional)" —
 * Controller'dan gelen iş süreçlerini karşılar, DAO'yu çağırır. Bu test seti
 * proje gereksinim belgesindeki "JUnit ile açıklamalı birim testler"
 * maddesini login/register akışı için karşılar.</p>
 *
 * @author splitnorder team — BM470
 */
public class AuthServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ============================================================
    // loginLocal() testleri
    // ============================================================

    /**
     * Geçerli LOCAL kullanıcı + doğru şifre + doğrulanmış email → User döner.
     * Bu, mutlu yolun (happy path) baz davranışını sabitler.
     */
    @Test
    public void loginLocal_validCredentials_returnsUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("a@b.com");
        user.setUsername("alice");
        user.setAuthProvider("LOCAL");
        user.setPasswordHash(PasswordHasher.sha256("secret123"));
        user.setEmailVerified(true);

        when(userDao.findByEmail("a@b.com")).thenReturn(user);

        User result = authService.loginLocal("a@b.com", "secret123");

        assertNotNull(result);
        assertEquals("alice", result.getUsername());
    }

    /**
     * Email DB'de yoksa Service {@link InvalidCredentialsException} fırlatır.
     * Generic exception değil — Controller bu tipi özellikle yakalar.
     */
    @Test
    public void loginLocal_userNotFound_throwsInvalidCredentials() {
        when(userDao.findByEmail("ghost@b.com")).thenReturn(null);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.loginLocal("ghost@b.com", "x"));
    }

    /**
     * Hesap GOOGLE provider'lı ise LOCAL login denenirse Service
     * {@link InvalidCredentialsException} fırlatır. Kullanıcı Google
     * akışını kullanmak zorundadır; LOCAL kanalda parola yoktur.
     */
    @Test
    public void loginLocal_googleUser_throwsInvalidCredentials() {
        User user = new User();
        user.setEmail("g@b.com");
        user.setAuthProvider("GOOGLE");
        when(userDao.findByEmail("g@b.com")).thenReturn(user);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.loginLocal("g@b.com", "any"));
    }

    /**
     * Yanlış parola → {@link InvalidCredentialsException}. SHA-256 hash
     * karşılaştırması başarısız olduğunda email-not-verified kontrolüne
     * bile ulaşılmaz.
     */
    @Test
    public void loginLocal_wrongPassword_throwsInvalidCredentials() {
        User user = new User();
        user.setEmail("a@b.com");
        user.setAuthProvider("LOCAL");
        user.setPasswordHash(PasswordHasher.sha256("correct"));
        user.setEmailVerified(true);
        when(userDao.findByEmail("a@b.com")).thenReturn(user);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.loginLocal("a@b.com", "wrong"));
    }

    /**
     * Şifre doğru ama email doğrulanmamışsa Service
     * {@link EmailNotVerifiedException} fırlatır. Controller bu tipi ayrı
     * yakalar ve kullanıcıya "doğrulama maili kontrol et" mesajı gösterir.
     * <b>Önemli:</b> Bu davranışı eski kod {@code INVALID_CREDENTIALS}
     * olarak gösteriyordu — bug regression testidir.
     */
    @Test
    public void loginLocal_emailNotVerified_throwsEmailNotVerified() {
        User user = new User();
        user.setEmail("a@b.com");
        user.setAuthProvider("LOCAL");
        user.setPasswordHash(PasswordHasher.sha256("secret"));
        user.setEmailVerified(false);
        when(userDao.findByEmail("a@b.com")).thenReturn(user);

        assertThrows(EmailNotVerifiedException.class,
                () -> authService.loginLocal("a@b.com", "secret"));
    }

    // ============================================================
    // registerLocal() testleri
    // ============================================================

    /**
     * Username + email yoksa {@code save()} çağrılır, doğrulama token'ı
     * üretilir, email gönderimi denenir. Token boş olmamalı (UUID).
     */
    @Test
    public void registerLocal_newUser_savesAndReturnsToken() {
        when(userDao.findByUsername("bob")).thenReturn(null);
        when(userDao.findByEmail("b@c.com")).thenReturn(null);

        User result = authService.registerLocal("bob", "b@c.com", "pw", "tr");

        assertNotNull(result);
        assertEquals("bob", result.getUsername());
        assertEquals("b@c.com", result.getEmail());
        assertEquals("LOCAL", result.getAuthProvider());
        assertFalse(result.isEmailVerified());
        assertNotNull(result.getVerificationToken());
        assertNotNull(result.getVerificationTokenExpiry());
        verify(userDao).save(any(User.class));
    }

    /**
     * Aynı username zaten varsa → {@link UsernameExistsException}.
     * Email DAO çağrısına bile gidilmez (kısa devre).
     */
    @Test
    public void registerLocal_duplicateUsername_throwsUsernameExists() {
        User existing = new User();
        existing.setUsername("bob");
        when(userDao.findByUsername("bob")).thenReturn(existing);

        assertThrows(UsernameExistsException.class,
                () -> authService.registerLocal("bob", "b@c.com", "pw", "tr"));

        verify(userDao, never()).save(any());
    }

    /**
     * Aynı email zaten varsa → {@link EmailExistsException}.
     * Save çağrılmaz, mail gönderilmez.
     */
    @Test
    public void registerLocal_duplicateEmail_throwsEmailExists() {
        when(userDao.findByUsername("bob")).thenReturn(null);
        User existing = new User();
        existing.setEmail("b@c.com");
        when(userDao.findByEmail("b@c.com")).thenReturn(existing);

        assertThrows(EmailExistsException.class,
                () -> authService.registerLocal("bob", "b@c.com", "pw", "tr"));

        verify(userDao, never()).save(any());
    }

    // ============================================================
    // verifyEmail() testleri
    // ============================================================

    /**
     * Geçerli token + süresi dolmamış → kullanıcının {@code emailVerified}
     * alanı {@code true}'ya çekilir, token + expiry temizlenir, update edilir.
     */
    @Test
    public void verifyEmail_validToken_marksUserVerified() {
        User user = new User();
        user.setEmail("a@b.com");
        user.setEmailVerified(false);
        user.setVerificationToken("tok-1");
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(1));
        when(userDao.findByVerificationToken("tok-1")).thenReturn(user);

        authService.verifyEmail("tok-1");

        assertTrue(user.isEmailVerified());
        assertNull(user.getVerificationToken());
        assertNull(user.getVerificationTokenExpiry());
        verify(userDao).update(user);
    }

    /**
     * Süresi dolmuş token → {@link VerificationTokenExpiredException}, exception
     * kullanıcının email'ini taşır. Bu, Controller'ın login sayfasına
     * {@code ?error=TOKEN_EXPIRED&email=...} redirect edip kullanıcıya
     * tekrar-gönder akışını sunabilmesi için kritik.
     */
    @Test
    public void verifyEmail_expiredToken_throwsExpiredWithEmail() {
        User user = new User();
        user.setEmail("expired@b.com");
        user.setVerificationToken("old-tok");
        user.setVerificationTokenExpiry(LocalDateTime.now().minusHours(1));
        when(userDao.findByVerificationToken("old-tok")).thenReturn(user);

        VerificationTokenExpiredException ex = assertThrows(
                VerificationTokenExpiredException.class,
                () -> authService.verifyEmail("old-tok"));

        assertEquals("expired@b.com", ex.getEmail());
        verify(userDao, never()).update(any());
    }

    /**
     * Hiç eşleşmeyen token → {@link InvalidTokenException}. Bu durumda
     * email bilgisi yok (kullanıcı tespit edilemedi), Controller jenerik
     * INVALID_TOKEN hatasıyla login sayfasına yönlendirir.
     */
    @Test
    public void verifyEmail_unknownToken_throwsInvalidToken() {
        when(userDao.findByVerificationToken("ghost")).thenReturn(null);

        assertThrows(InvalidTokenException.class,
                () -> authService.verifyEmail("ghost"));
    }

    // ============================================================
    // resendVerificationEmail() testleri
    // ============================================================

    /**
     * Doğrulanmamış kullanıcı için resend → yeni UUID token üretilir, expiry
     * 24h ileri kaydırılır, {@code EmailService.sendVerificationEmail}
     * çağrılır. Önceki token override edilir (eski link otomatik invalidate).
     */
    @Test
    public void resendVerification_unverifiedUser_rotatesTokenAndSendsEmail() {
        User user = new User();
        user.setEmail("u@b.com");
        user.setUsername("u");
        user.setEmailVerified(false);
        user.setVerificationToken("eski-tok");
        when(userDao.findByEmail("u@b.com")).thenReturn(user);

        authService.resendVerificationEmail("u@b.com", "tr");

        assertNotNull(user.getVerificationToken());
        assertNotEquals("eski-tok", user.getVerificationToken());
        assertNotNull(user.getVerificationTokenExpiry());
        assertTrue(user.getVerificationTokenExpiry().isAfter(LocalDateTime.now()));
        verify(userDao).update(user);
        verify(emailService).sendVerificationEmail(eq("u@b.com"), eq("u"),
                eq(user.getVerificationToken()), eq("tr"));
    }

    /**
     * Zaten doğrulanmış kullanıcı için resend → no-op (sessizce döner).
     * Mail gönderilmez, token rotate edilmez. Bu, doğrulanmış kullanıcının
     * yanlışlıkla resend tetiklemesi durumunda kafa karıştırıcı yeni mail
     * almasını engeller.
     */
    @Test
    public void resendVerification_alreadyVerifiedUser_isNoop() {
        User user = new User();
        user.setEmail("ok@b.com");
        user.setEmailVerified(true);
        when(userDao.findByEmail("ok@b.com")).thenReturn(user);

        authService.resendVerificationEmail("ok@b.com", "tr");

        verify(userDao, never()).update(any());
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString(), anyString(), anyString());
    }

    /**
     * Hiç olmayan email için resend → {@link UserNotFoundException}.
     * Controller bu durumda kullanıcıya genel hata gösterir; sessiz geçilseydi
     * UI yanlış "mail gönderildi" mesajı verirdi.
     */
    @Test
    public void resendVerification_userNotFound_throwsUserNotFound() {
        when(userDao.findByEmail("ghost@b.com")).thenReturn(null);

        assertThrows(UserNotFoundException.class,
                () -> authService.resendVerificationEmail("ghost@b.com", "tr"));
    }
}
