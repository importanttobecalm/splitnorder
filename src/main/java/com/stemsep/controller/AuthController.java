package com.stemsep.controller;

import com.stemsep.exception.EmailExistsException;
import com.stemsep.exception.EmailNotVerifiedException;
import com.stemsep.exception.InvalidCredentialsException;
import com.stemsep.exception.InvalidTokenException;
import com.stemsep.exception.UsernameExistsException;
import com.stemsep.exception.VerificationTokenExpiredException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.stemsep.model.User;
import com.stemsep.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "message", required = false) String message,
                            @RequestParam(value = "email", required = false) String email,
                            Model model) {
        if (error != null) model.addAttribute("error", error);
        if (message != null) model.addAttribute("message", message);
        if (email != null) model.addAttribute("email", email);
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/login")
    public String login(@RequestParam("email") String email,
                        @RequestParam("password") String password,
                        HttpSession session,
                        Model model) {
        try {
            User user = authService.loginLocal(email.trim().toLowerCase(), password);
            session.setAttribute("user", user);
            return "redirect:/";
        } catch (InvalidCredentialsException e) {
            logger.warn("Login başarısız (geçersiz kimlik): email={}", email);
            model.addAttribute("error", "INVALID_CREDENTIALS");
            model.addAttribute("email", email);
            return "auth/login";
        } catch (EmailNotVerifiedException e) {
            logger.warn("Login başarısız (email doğrulanmamış): email={}", email);
            model.addAttribute("error", "EMAIL_NOT_VERIFIED");
            model.addAttribute("email", email);
            return "auth/login";
        } catch (RuntimeException e) {
            logger.error("Login sırasında beklenmedik hata: email={}", email, e);
            model.addAttribute("error", "INTERNAL_ERROR");
            model.addAttribute("email", email);
            return "auth/login";
        }
    }

    @PostMapping("/register")
    public String register(@RequestParam("username") String username,
                           @RequestParam("email") String email,
                           @RequestParam("password") String password,
                           @RequestParam(value = "lang", defaultValue = "tr") String lang,
                           Model model) {
        try {
            authService.registerLocal(username.trim(), email.trim().toLowerCase(), password, lang);
            return "redirect:/auth/login?message=REGISTRATION_SUCCESS";
        } catch (UsernameExistsException e) {
            logger.warn("Kayıt başarısız (kullanıcı adı dolu): username={}", username);
            model.addAttribute("error", "USERNAME_EXISTS");
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "auth/register";
        } catch (EmailExistsException e) {
            logger.warn("Kayıt başarısız (email dolu): email={}", email);
            model.addAttribute("error", "EMAIL_EXISTS");
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "auth/register";
        } catch (RuntimeException e) {
            logger.error("Register sırasında beklenmedik hata: email={}", email, e);
            model.addAttribute("error", "INTERNAL_ERROR");
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "auth/register";
        }
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token) {
        try {
            authService.verifyEmail(token);
            return "redirect:/auth/login?message=EMAIL_VERIFIED";
        } catch (VerificationTokenExpiredException e) {
            logger.warn("E-posta doğrulama: token süresi dolmuş, kullanıcıya resend akışı sunulacak (email={})", e.getEmail());
            String encoded = URLEncoder.encode(e.getEmail(), StandardCharsets.UTF_8);
            return "redirect:/auth/login?error=TOKEN_EXPIRED&email=" + encoded;
        } catch (InvalidTokenException e) {
            logger.warn("E-posta doğrulama: geçersiz token");
            return "redirect:/auth/login?error=INVALID_TOKEN";
        }
    }

    @PostMapping("/resend-verification")
    public String resendVerification(@RequestParam("email") String email,
                                     @RequestParam(value = "lang", defaultValue = "tr") String lang) {
        authService.resendVerificationEmail(email.trim().toLowerCase(), lang);
        return "redirect:/auth/login?message=VERIFICATION_RESENT";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam("email") String email,
                                 @RequestParam(value = "lang", defaultValue = "tr") String lang) {
        authService.requestPasswordReset(email.trim().toLowerCase(), lang);
        return "redirect:/auth/login?message=RESET_LINK_SENT";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("token") String token,
                                @RequestParam("password") String password,
                                @RequestParam("password_confirm") String passwordConfirm,
                                Model model) {
        if (password == null || password.length() < 8) {
            model.addAttribute("error", "PASSWORD_TOO_SHORT");
            model.addAttribute("token", token);
            return "auth/reset-password";
        }
        if (!password.equals(passwordConfirm)) {
            model.addAttribute("error", "PASSWORD_MISMATCH");
            model.addAttribute("token", token);
            return "auth/reset-password";
        }
        try {
            authService.resetPassword(token, password);
            return "redirect:/auth/login?message=PASSWORD_RESET_SUCCESS";
        } catch (InvalidTokenException e) {
            logger.warn("Parola sıfırlama başarısız (geçersiz/süresi dolmuş token)");
            model.addAttribute("error", "INVALID_OR_EXPIRED_TOKEN");
            model.addAttribute("token", token);
            return "auth/reset-password";
        } catch (RuntimeException e) {
            logger.error("Parola sıfırlama sırasında beklenmedik hata", e);
            model.addAttribute("error", "INTERNAL_ERROR");
            model.addAttribute("token", token);
            return "auth/reset-password";
        }
    }
}
