package com.stemsep.controller;

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
    public String loginPage() {
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
        } catch (RuntimeException e) {
            logger.warn("Login başarısız: email={}, error={}", email, e.getClass().getSimpleName());
            model.addAttribute("error", "INVALID_CREDENTIALS");
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
        } catch (RuntimeException e) {
            logger.warn("Kayıt başarısız: email={}, error={}", email, e.getClass().getSimpleName());
            String code = e.getClass().getSimpleName().replace("Exception", "").toUpperCase();
            model.addAttribute("error", code);
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "auth/register";
        }
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token) {
        authService.verifyEmail(token);
        return "redirect:/auth/login?message=EMAIL_VERIFIED";
    }

    @PostMapping("/resend-verification")
    public String resendVerification(@RequestParam("email") String email,
                                     @RequestParam(value = "lang", defaultValue = "tr") String lang) {
        authService.resendVerificationEmail(email.trim().toLowerCase(), lang);
        return "redirect:/auth/login?message=VERIFICATION_RESENT";
    }
}
