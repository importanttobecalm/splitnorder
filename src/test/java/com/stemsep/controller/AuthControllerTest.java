package com.stemsep.controller;

import com.stemsep.exception.EmailExistsException;
import com.stemsep.exception.EmailNotVerifiedException;
import com.stemsep.exception.InvalidCredentialsException;
import com.stemsep.exception.InvalidTokenException;
import com.stemsep.exception.UsernameExistsException;
import com.stemsep.exception.VerificationTokenExpiredException;
import com.stemsep.model.User;
import com.stemsep.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * {@link AuthController} için MockMvc tabanlı birim testleri.
 *
 * <p>Service katmanı mock'lanır; sadece Controller'ın exception tipini doğru
 * UPPER_SNAKE_CASE message key'ine eşleyip eşlemediği, başarı yolunda redirect
 * + session attribute set'inin doğru yapılıp yapılmadığı doğrulanır.</p>
 *
 * <p><b>Bug regression notu:</b> Eski kod tüm {@link RuntimeException}'ı tek
 * sabit {@code "INVALID_CREDENTIALS"} key'ine eşliyordu; bu yüzden email
 * doğrulanmamış kullanıcı yanlış mesaj alıyor, DB hataları gizleniyor ve
 * properties'te olmayan key {@code ???auth.error.INVALID_CREDENTIALS???}
 * şeklinde JSP'ye sızıyordu. Bu test sınıfı o davranışın tekrar etmesini
 * önler.</p>
 *
 * @author splitnorder team — BM470
 */
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController controller;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ============================================================
    // POST /auth/login
    // ============================================================

    /**
     * Service geçerli User döndürürse Controller {@code /} adresine
     * yönlendirir ve session'a {@code user} attribute'unu yerleştirir.
     */
    @Test
    public void loginPost_validCredentials_redirectsHome() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("a@b.com");
        when(authService.loginLocal("a@b.com", "secret")).thenReturn(user);

        mockMvc.perform(post("/auth/login")
                        .param("email", "a@b.com")
                        .param("password", "secret"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(request().sessionAttribute("user", user));
    }

    /**
     * Service {@link InvalidCredentialsException} fırlatırsa Controller
     * "auth/login" view'ını döner ve {@code error="INVALID_CREDENTIALS"}
     * koyar (UPPER_SNAKE_CASE — JSP'nin beklediği convention).
     */
    @Test
    public void loginPost_invalidCredentials_returnsLoginViewWithError() throws Exception {
        when(authService.loginLocal(anyString(), anyString()))
                .thenThrow(new InvalidCredentialsException("a@b.com"));

        mockMvc.perform(post("/auth/login")
                        .param("email", "a@b.com")
                        .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute("error", "INVALID_CREDENTIALS"));
    }

    /**
     * Email doğrulanmamış kullanıcı: hata key'i {@code EMAIL_NOT_VERIFIED}.
     * <b>Eski buggy kod bu testte INVALID_CREDENTIALS koyuyordu.</b>
     */
    @Test
    public void loginPost_emailNotVerified_returnsLoginViewWithEmailNotVerifiedError() throws Exception {
        when(authService.loginLocal(anyString(), anyString()))
                .thenThrow(new EmailNotVerifiedException("a@b.com"));

        mockMvc.perform(post("/auth/login")
                        .param("email", "a@b.com")
                        .param("password", "secret"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute("error", "EMAIL_NOT_VERIFIED"));
    }

    /**
     * Beklenmedik bir {@link RuntimeException} (örn. DB down) →
     * {@code INTERNAL_ERROR}. Eski kod bunu da INVALID_CREDENTIALS
     * olarak gösteriyor ve debug'ı imkansızlaştırıyordu.
     */
    @Test
    public void loginPost_unexpectedException_returnsLoginViewWithInternalError() throws Exception {
        when(authService.loginLocal(anyString(), anyString()))
                .thenThrow(new RuntimeException("db down"));

        mockMvc.perform(post("/auth/login")
                        .param("email", "a@b.com")
                        .param("password", "secret"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute("error", "INTERNAL_ERROR"));
    }

    // ============================================================
    // POST /auth/register
    // ============================================================

    /**
     * Başarılı kayıt: 302 redirect, location includes "REGISTRATION_SUCCESS".
     * Service hiç exception fırlatmaz.
     */
    @Test
    public void registerPost_success_redirectsToLoginWithMessage() throws Exception {
        when(authService.registerLocal(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new User());

        mockMvc.perform(post("/auth/register")
                        .param("username", "bob")
                        .param("email", "b@c.com")
                        .param("password", "pw12345A"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login?message=REGISTRATION_SUCCESS"));
    }

    /**
     * Username çakışması → {@code USERNAME_EXISTS} (UPPER_SNAKE_CASE).
     * Eski kod {@code "USERNAMEEXISTS"} üretiyordu — properties'teki
     * {@code auth.error.USERNAME_EXISTS} key'ine asla map olmuyordu.
     */
    @Test
    public void registerPost_duplicateUsername_returnsRegisterViewWithError() throws Exception {
        when(authService.registerLocal(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new UsernameExistsException("bob"));

        mockMvc.perform(post("/auth/register")
                        .param("username", "bob")
                        .param("email", "b@c.com")
                        .param("password", "pw"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attribute("error", "USERNAME_EXISTS"));
    }

    /**
     * Email çakışması → {@code EMAIL_EXISTS} (UPPER_SNAKE_CASE).
     */
    @Test
    public void registerPost_duplicateEmail_returnsRegisterViewWithError() throws Exception {
        when(authService.registerLocal(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new EmailExistsException("b@c.com"));

        mockMvc.perform(post("/auth/register")
                        .param("username", "bob")
                        .param("email", "b@c.com")
                        .param("password", "pw"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attribute("error", "EMAIL_EXISTS"));
    }

    // ============================================================
    // GET /auth/verify-email — token süresi / geçersizlik akışı
    // ============================================================

    /**
     * Geçerli token → Service başarıyla doğrular, Controller login sayfasına
     * {@code ?message=EMAIL_VERIFIED} ile redirect eder. Kullanıcıya pozitif
     * geri bildirim (info kutusu) gösterilir.
     */
    @Test
    public void verifyEmailGet_validToken_redirectsLoginWithMessage() throws Exception {
        mockMvc.perform(get("/auth/verify-email").param("token", "tok-ok"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login?message=EMAIL_VERIFIED"));
    }

    /**
     * Süresi dolmuş token → Controller {@link VerificationTokenExpiredException}'ı
     * yakalar ve login sayfasına {@code ?error=TOKEN_EXPIRED&email=<urlencoded>}
     * ile yönlendirir. Email URL-encode edilmiş olmalı (özel karakter güvenliği).
     * Bu redirect, login.jsp'deki resend formunun email'i bilmesini sağlar.
     */
    @Test
    public void verifyEmailGet_expiredToken_redirectsLoginWithTokenExpiredAndEmail() throws Exception {
        doThrow(new VerificationTokenExpiredException("user+test@b.com"))
                .when(authService).verifyEmail("tok-old");

        mockMvc.perform(get("/auth/verify-email").param("token", "tok-old"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login?error=TOKEN_EXPIRED&email=user%2Btest%40b.com"));
    }

    /**
     * Hiç eşleşmeyen token (örn. tampered link) → Controller
     * {@link InvalidTokenException}'ı yakalar; email bilgisi yok, sadece
     * {@code ?error=INVALID_TOKEN} ile login'e döner. JSP'de bu hatada
     * resend formu görünmez (email param boş olduğu için).
     */
    @Test
    public void verifyEmailGet_invalidToken_redirectsLoginWithInvalidTokenError() throws Exception {
        doThrow(new InvalidTokenException("ghost"))
                .when(authService).verifyEmail("ghost");

        mockMvc.perform(get("/auth/verify-email").param("token", "ghost"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login?error=INVALID_TOKEN"));
    }

    // ============================================================
    // POST /auth/resend-verification
    // ============================================================

    /**
     * Resend endpoint başarılı çalışırsa Service'i çağırır ve login sayfasına
     * {@code ?message=VERIFICATION_RESENT} ile redirect eder. Email
     * normalize edilmeli (trim + lowercase) — Service'e o şekilde geçer.
     */
    @Test
    public void resendVerification_redirectsLoginWithMessage() throws Exception {
        mockMvc.perform(post("/auth/resend-verification")
                        .param("email", "  USER@B.com ")
                        .param("lang", "tr"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login?message=VERIFICATION_RESENT"));

        verify(authService).resendVerificationEmail("user@b.com", "tr");
    }

    // ============================================================
    // GET /auth/login — query param binding (redirect-after-error akışı)
    // ============================================================

    /**
     * Login GET'inde {@code error} ve {@code email} query parametreleri
     * model'e bağlanmalı. Bu olmadan verify-email redirect'inden gelen
     * kullanıcı login.jsp'de hata kutusunu ve resend butonunu göremezdi
     * (eski sürümde GET handler param bağlamıyordu).
     */
    @Test
    public void loginGet_withErrorAndEmailParams_populatesModel() throws Exception {
        mockMvc.perform(get("/auth/login")
                        .param("error", "TOKEN_EXPIRED")
                        .param("email", "a@b.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute("error", "TOKEN_EXPIRED"))
                .andExpect(model().attribute("email", "a@b.com"));
    }

    /**
     * Login GET'inde {@code message} query parametresi de model'e bağlanır
     * (örn. {@code ?message=VERIFICATION_RESENT}). Bu olmadan resend
     * sonrasında kullanıcıya "mail tekrar gönderildi" info kutusu görünmezdi.
     */
    @Test
    public void loginGet_withMessageParam_populatesModel() throws Exception {
        mockMvc.perform(get("/auth/login").param("message", "VERIFICATION_RESENT"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute("message", "VERIFICATION_RESENT"));
    }
}
