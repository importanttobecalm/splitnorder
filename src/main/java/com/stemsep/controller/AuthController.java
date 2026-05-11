package com.stemsep.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stemsep.model.User;
import com.stemsep.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Kimlik doğrulama Controller'ı (JSP form-based).
 * Login, Register, Logout, Profile, e-posta doğrulama ve Google OAuth.
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$"
    );

    @Autowired
    private AuthService authService;

    @Autowired
    private Environment env;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ======================== LOGIN ========================

    @GetMapping("/login")
    public String showLogin(HttpSession session,
                            @RequestParam(value = "registered", required = false) String registered,
                            @RequestParam(value = "verified", required = false) String verified,
                            @RequestParam(value = "resent", required = false) String resent,
                            @RequestParam(value = "error", required = false) String error,
                            Model model) {
        if (session.getAttribute("user") != null) {
            return "redirect:/";
        }
        if (registered != null) model.addAttribute("info", "auth.info.registered");
        if (verified != null)   model.addAttribute("info", "auth.info.verified");
        if (resent != null)     model.addAttribute("info", "auth.info.resent");
        if (error != null)      model.addAttribute("error", error);
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam("email") String email,
                          @RequestParam("password") String password,
                          HttpSession session,
                          RedirectAttributes ra,
                          Model model) {
        try {
            String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
            if (normalizedEmail.isEmpty()) {
                model.addAttribute("error", "EMAIL_REQUIRED");
                model.addAttribute("email", normalizedEmail);
                return "login";
            }
            if (password == null || password.isEmpty()) {
                model.addAttribute("error", "PASSWORD_REQUIRED");
                model.addAttribute("email", normalizedEmail);
                return "login";
            }

            User user = authService.loginLocal(normalizedEmail, password);
            session.setAttribute("user", user);
            return "redirect:/";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("email", email == null ? "" : email.trim());
            return "login";
        } catch (Exception e) {
            logger.error("Giriş sırasında beklenmeyen hata", e);
            model.addAttribute("error", "INTERNAL_ERROR");
            return "login";
        }
    }

    // ======================== REGISTER ========================

    @GetMapping("/register")
    public String showRegister(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/";
        }
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam("username") String username,
                             @RequestParam("email") String email,
                             @RequestParam("password") String password,
                             @RequestParam(value = "lang", defaultValue = "tr") String lang,
                             RedirectAttributes ra,
                             Model model) {
        try {
            String trimmedUsername = username == null ? "" : username.trim();
            String normalizedEmail = email == null ? "" : email.trim().toLowerCase();

            if (trimmedUsername.isEmpty()) {
                return registerError(model, trimmedUsername, normalizedEmail, "USERNAME_REQUIRED");
            }
            if (normalizedEmail.isEmpty() || !isValidEmail(normalizedEmail)) {
                return registerError(model, trimmedUsername, normalizedEmail, "INVALID_EMAIL");
            }
            if (password == null || password.length() < 8) {
                return registerError(model, trimmedUsername, normalizedEmail, "PASSWORD_TOO_SHORT");
            }
            if (!isPasswordStrong(password)) {
                return registerError(model, trimmedUsername, normalizedEmail, "PASSWORD_WEAK");
            }

            authService.registerLocal(trimmedUsername, normalizedEmail, password, lang);
            ra.addAttribute("registered", "1");
            return "redirect:/auth/login";

        } catch (IllegalArgumentException e) {
            return registerError(model, username, email, e.getMessage());
        } catch (Exception e) {
            logger.error("Kayıt sırasında beklenmeyen hata", e);
            return registerError(model, username, email, "INTERNAL_ERROR");
        }
    }

    private String registerError(Model model, String username, String email, String code) {
        model.addAttribute("error", code);
        model.addAttribute("username", username);
        model.addAttribute("email", email);
        return "register";
    }

    // ======================== LOGOUT ========================

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/auth/login";
    }

    // ======================== PROFILE ========================

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("user", user);
        return "profile";
    }

    // ======================== EMAIL VERIFICATION ========================

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token, Model model) {
        boolean verified = authService.verifyEmail(token);
        if (verified) {
            return "redirect:/auth/login?verified=1";
        }
        model.addAttribute("error", "INVALID_OR_EXPIRED_TOKEN");
        return "verify-email";
    }

    @PostMapping("/resend-verification")
    public String resendVerification(@RequestParam("email") String email,
                                     @RequestParam(value = "lang", defaultValue = "tr") String lang,
                                     RedirectAttributes ra) {
        try {
            String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
            if (normalizedEmail.isEmpty()) {
                ra.addAttribute("error", "EMAIL_REQUIRED");
                return "redirect:/auth/login";
            }
            authService.resendVerificationEmail(normalizedEmail, lang);
            ra.addAttribute("resent", "1");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            ra.addAttribute("error", e.getMessage());
            return "redirect:/auth/login";
        } catch (Exception e) {
            logger.error("Doğrulama maili tekrar gönderilemedi", e);
            ra.addAttribute("error", "INTERNAL_ERROR");
            return "redirect:/auth/login";
        }
    }

    // ======================== GOOGLE OAUTH ========================

    @GetMapping("/google/login")
    public void redirectToGoogle(HttpServletResponse response) throws IOException {
        String clientId = env.getProperty("GOOGLE_CLIENT_ID");
        String redirectUri = env.getProperty("GOOGLE_REDIRECT_URI");
        String scope = "openid email profile";

        if (clientId == null || redirectUri == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Google OAuth ayarları eksik.");
            return;
        }

        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8) +
                "&access_type=offline" +
                "&prompt=consent";

        response.sendRedirect(authUrl);
    }

    @GetMapping("/google/callback")
    public void googleCallback(@RequestParam(value = "code", required = false) String code,
                               HttpServletRequest request,
                               HttpServletResponse response,
                               HttpSession session) throws IOException {

        String ctx = request.getContextPath();

        if (code == null || code.trim().isEmpty()) {
            response.sendRedirect(ctx + "/auth/login?error=GOOGLE_AUTH_FAILED");
            return;
        }

        try {
            String clientId = env.getProperty("GOOGLE_CLIENT_ID");
            String clientSecret = env.getProperty("GOOGLE_CLIENT_SECRET");
            String redirectUri = env.getProperty("GOOGLE_REDIRECT_URI");

            String tokenResponse = exchangeCodeForToken(code, clientId, clientSecret, redirectUri);
            JsonNode tokenJson = objectMapper.readTree(tokenResponse);
            String accessToken = tokenJson.get("access_token").asText();

            String userInfoResponse = fetchGoogleUserInfo(accessToken);
            JsonNode userInfo = objectMapper.readTree(userInfoResponse);

            String googleId = userInfo.get("id").asText();
            String email = userInfo.get("email").asText();
            String name = userInfo.has("name") ? userInfo.get("name").asText() : email.split("@")[0];
            String picture = userInfo.has("picture") ? userInfo.get("picture").asText() : null;

            User user = authService.loginOrRegisterGoogle(googleId, email, name, picture);
            session.setAttribute("user", user);

            response.sendRedirect(ctx + "/");

        } catch (Exception e) {
            logger.error("Google login callback hatası", e);
            response.sendRedirect(ctx + "/auth/login?error=GOOGLE_AUTH_FAILED");
        }
    }

    // ======================== HELPERS ========================

    private String exchangeCodeForToken(String code, String clientId, String clientSecret, String redirectUri) throws Exception {
        URL url = new URL("https://oauth2.googleapis.com/token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);

        String params = "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&grant_type=authorization_code";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(params.getBytes(StandardCharsets.UTF_8));
        }

        try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    private String fetchGoogleUserInfo(String accessToken) throws Exception {
        URL url = new URL("https://www.googleapis.com/oauth2/v2/userinfo");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);

        try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.length() > 254) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isPasswordStrong(String password) {
        if (password.length() < 8) return false;
        boolean hasUpper = false, hasLower = false, hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        return hasUpper && hasLower && hasDigit;
    }
}
