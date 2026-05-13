package com.stemsep.controller;

import com.stemsep.exception.EmailExistsException;
import com.stemsep.exception.EmailNotVerifiedException;
import com.stemsep.exception.InvalidCredentialsException;
import com.stemsep.exception.UsernameExistsException;
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
import static org.mockito.Mockito.when;
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
}
