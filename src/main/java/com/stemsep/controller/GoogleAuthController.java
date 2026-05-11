package com.stemsep.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stemsep.exception.GoogleAuthException;
import com.stemsep.model.User;
import com.stemsep.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Controller
@RequestMapping("/api/auth")
public class GoogleAuthController {

    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AuthService authService;

    @Autowired
    private Environment env;

    @GetMapping("/google/login")
    public void redirectToGoogle(HttpServletResponse response) throws IOException {
        String clientId = env.getProperty("GOOGLE_CLIENT_ID");
        String redirectUri = env.getProperty("GOOGLE_REDIRECT_URI");
        if (clientId == null || redirectUri == null) {
            throw new GoogleAuthException("Google OAuth ayarları eksik");
        }
        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth?"
                + "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode("openid email profile", StandardCharsets.UTF_8)
                + "&access_type=offline&prompt=consent";
        response.sendRedirect(authUrl);
    }

    @GetMapping("/google/callback")
    public String googleCallback(@RequestParam(value = "code", required = false) String code,
                                 HttpSession session) {
        if (code == null || code.isBlank()) {
            throw new GoogleAuthException("Google authorization code yok");
        }
        try {
            String clientId = env.getProperty("GOOGLE_CLIENT_ID");
            String clientSecret = env.getProperty("GOOGLE_CLIENT_SECRET");
            String redirectUri = env.getProperty("GOOGLE_REDIRECT_URI");

            JsonNode tokenJson = objectMapper.readTree(exchangeCodeForToken(code, clientId, clientSecret, redirectUri));
            String accessToken = tokenJson.get("access_token").asText();

            JsonNode userInfo = objectMapper.readTree(fetchGoogleUserInfo(accessToken));
            String googleId = userInfo.get("id").asText();
            String email = userInfo.get("email").asText();
            String name = userInfo.has("name") ? userInfo.get("name").asText() : email.split("@")[0];
            String picture = userInfo.has("picture") ? userInfo.get("picture").asText() : null;

            User user = authService.loginOrRegisterGoogle(googleId, email, name, picture);
            session.setAttribute("user", user);
            return "redirect:/";
        } catch (IOException e) {
            logger.error("Google callback hatası", e);
            throw new GoogleAuthException(e.getMessage());
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) session.invalidate();
        return "redirect:/auth/login";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/auth/login";
        model.addAttribute("user", user);
        return "profile";
    }

    private String exchangeCodeForToken(String code, String clientId, String clientSecret, String redirectUri) throws IOException {
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
        try (Scanner s = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
            s.useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }
    }

    private String fetchGoogleUserInfo(String accessToken) throws IOException {
        URL url = new URL("https://www.googleapis.com/oauth2/v2/userinfo");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        try (Scanner s = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
            s.useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }
    }
}
