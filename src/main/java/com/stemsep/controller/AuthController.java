package com.stemsep.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stemsep.model.User;
import com.stemsep.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Kimlik doğrulama REST Controller'ı.
 * Login, Register, Google OAuth ve e-posta doğrulama endpoint'leri.
 */
@Controller
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private Environment env;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ======================== LOCAL REGISTER ========================

    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();

        try {
            String username = body.get("username");
            String email = body.get("email");
            String password = body.get("password");
            String lang = body.getOrDefault("lang", "tr");

            // Validasyon
            if (username == null || username.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "USERNAME_REQUIRED");
                return ResponseEntity.badRequest().body(response);
            }
            if (email == null || email.trim().isEmpty() || !isValidEmail(email.trim())) {
                response.put("success", false);
                response.put("error", "INVALID_EMAIL");
                return ResponseEntity.badRequest().body(response);
            }
            if (password == null || password.length() < 8) {
                response.put("success", false);
                response.put("error", "PASSWORD_TOO_SHORT");
                return ResponseEntity.badRequest().body(response);
            }
            // Şifre güvenlik kontrolü
            if (!isPasswordStrong(password)) {
                response.put("success", false);
                response.put("error", "PASSWORD_WEAK");
                return ResponseEntity.badRequest().body(response);
            }

            User user = authService.registerLocal(username.trim(), email.trim().toLowerCase(), password, lang);

            response.put("success", true);
            response.put("message", "REGISTRATION_SUCCESS");
            response.put("emailVerificationRequired", true);
            response.put("user", buildUserResponse(user));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Kayıt sırasında beklenmeyen hata", e);
            response.put("success", false);
            response.put("error", "INTERNAL_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ======================== LOCAL LOGIN ========================

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = body.get("email");
            String password = body.get("password");

            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "EMAIL_REQUIRED");
                return ResponseEntity.badRequest().body(response);
            }
            if (password == null || password.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "PASSWORD_REQUIRED");
                return ResponseEntity.badRequest().body(response);
            }

            User user = authService.loginLocal(email.trim().toLowerCase(), password);
            
            // Başarılı giriş - session oluştur (ÇOK ÖNEMLİ MANTIKSAL HATA DÜZELTMESİ)
            session.setAttribute("user", user);

            response.put("success", true);
            response.put("user", buildUserResponse(user));

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", e.getMessage());

            HttpStatus status = "EMAIL_NOT_VERIFIED".equals(e.getMessage())
                    ? HttpStatus.FORBIDDEN
                    : HttpStatus.UNAUTHORIZED;
            return ResponseEntity.status(status).body(response);

        } catch (Exception e) {
            logger.error("Giriş sırasında beklenmeyen hata", e);
            response.put("success", false);
            response.put("error", "INTERNAL_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        // React login sayfasına yönlendir (veya mevcut login yapınıza göre)
        return "redirect:http://localhost:5173/login";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Map<String, Object> model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:http://localhost:5173/login";
        }
        model.put("user", user);
        return "profile";
    }

    // ======================== EMAIL VERIFICATION ========================

    @GetMapping("/verify-email")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestParam("token") String token) {
        Map<String, Object> response = new HashMap<>();

        boolean verified = authService.verifyEmail(token);
        if (verified) {
            response.put("success", true);
            response.put("message", "EMAIL_VERIFIED");
        } else {
            response.put("success", false);
            response.put("error", "INVALID_OR_EXPIRED_TOKEN");
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resendVerification(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = body.get("email");
            String lang = body.getOrDefault("lang", "tr");

            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "EMAIL_REQUIRED");
                return ResponseEntity.badRequest().body(response);
            }

            authService.resendVerificationEmail(email.trim().toLowerCase(), lang);
            response.put("success", true);
            response.put("message", "VERIFICATION_RESENT");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Doğrulama maili tekrar gönderilemedi", e);
            response.put("success", false);
            response.put("error", "INTERNAL_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ======================== GOOGLE OAUTH (BACKEND REDIRECT) ========================

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
                               HttpServletResponse response,
                               HttpSession session) throws IOException {
        
        if (code == null || code.trim().isEmpty()) {
            response.sendRedirect("http://localhost:5173/login?error=GOOGLE_AUTH_FAILED");
            return;
        }

        try {
            String clientId = env.getProperty("GOOGLE_CLIENT_ID");
            String clientSecret = env.getProperty("GOOGLE_CLIENT_SECRET");
            String redirectUri = env.getProperty("GOOGLE_REDIRECT_URI");

            // Google token endpoint'ine POST
            String tokenResponse = exchangeCodeForToken(code, clientId, clientSecret, redirectUri);
            JsonNode tokenJson = objectMapper.readTree(tokenResponse);
            String accessToken = tokenJson.get("access_token").asText();

            // Access token ile kullanıcı bilgilerini al
            String userInfoResponse = fetchGoogleUserInfo(accessToken);
            JsonNode userInfo = objectMapper.readTree(userInfoResponse);

            String googleId = userInfo.get("id").asText();
            String email = userInfo.get("email").asText();
            String name = userInfo.has("name") ? userInfo.get("name").asText() : email.split("@")[0];
            String picture = userInfo.has("picture") ? userInfo.get("picture").asText() : null;

            User user = authService.loginOrRegisterGoogle(googleId, email, name, picture);
            
            // Başarılı giriş - session oluştur
            session.setAttribute("user", user);
            
            // JSP ana sayfaya yönlendir (Uygulamanın ana sayfası)
            response.sendRedirect("/stemsep/");
            
        } catch (Exception e) {
            logger.error("Google login callback hatası", e);
            response.sendRedirect("http://localhost:5173/login?error=GOOGLE_AUTH_FAILED");
        }
    }

    // ======================== YARDIMCI METODLAR ========================

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

    private Map<String, Object> buildUserResponse(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("email", user.getEmail());
        userMap.put("authProvider", user.getAuthProvider());
        userMap.put("emailVerified", user.isEmailVerified());
        userMap.put("profilePictureUrl", user.getProfilePictureUrl());
        userMap.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        return userMap;
    }

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$"
    );

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
