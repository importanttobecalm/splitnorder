package com.stemsep.service;

import com.stemsep.dao.UserDao;
import com.stemsep.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kimlik doğrulama servisi.
 * Kayıt, giriş, Google OAuth, e-posta doğrulama işlemleri.
 */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
    private static final int VERIFICATION_TOKEN_HOURS = 24;

    @Autowired
    private UserDao userDao;

    @Autowired
    private EmailService emailService;

    @Autowired
    private Environment env;

    /**
     * Normal (LOCAL) kayıt.
     * E-posta doğrulama token'ı oluşturur ve doğrulama maili gönderir.
     */
    @Transactional
    public User registerLocal(String username, String email, String password, String lang) {
        // Kullanıcı adı kontrolü
        if (userDao.findByUsername(username) != null) {
            throw new IllegalArgumentException("USERNAME_EXISTS");
        }

        // E-posta kontrolü
        User existingByEmail = userDao.findByEmail(email);
        if (existingByEmail != null) {
            throw new IllegalArgumentException("EMAIL_EXISTS");
        }

        // Şifre hash'le
        String hashedPassword = passwordEncoder.encode(password);

        // Doğrulama token'ı oluştur
        String verificationToken = UUID.randomUUID().toString();

        // Kullanıcı oluştur
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(hashedPassword);
        user.setAuthProvider("LOCAL");
        user.setEmailVerified(false);
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(VERIFICATION_TOKEN_HOURS));

        userDao.save(user);
        logger.info("Yeni kullanıcı kaydedildi: username={}, provider=LOCAL", username);

        // Doğrulama maili gönder (async değil — basitlik için senkron)
        try {
            emailService.sendVerificationEmail(email, username, verificationToken, lang);
        } catch (Exception e) {
            logger.error("Doğrulama maili gönderilemedi: email={}", email, e);
            // Kayıt başarılı oldu, mail gönderilemese de kullanıcı oluşturuldu
        }

        return user;
    }

    /**
     * Normal (LOCAL) giriş.
     */
    @Transactional(readOnly = true)
    public User loginLocal(String email, String password) {
        User user = userDao.findByEmail(email);

        if (user == null) {
            throw new IllegalArgumentException("USER_NOT_FOUND");
        }

        if (!"LOCAL".equals(user.getAuthProvider())) {
            throw new IllegalArgumentException("USE_GOOGLE_LOGIN");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("INVALID_PASSWORD");
        }

        if (!user.isEmailVerified()) {
            throw new IllegalArgumentException("EMAIL_NOT_VERIFIED");
        }

        logger.info("Kullanıcı giriş yaptı: id={}, username={}", user.getId(), user.getUsername());
        return user;
    }

    /**
     * Google OAuth ile giriş/kayıt.
     * Kullanıcı yoksa oluşturur, varsa günceller.
     */
    @Transactional
    public User loginOrRegisterGoogle(String googleId, String email, String name, String pictureUrl) {
        // Önce googleId ile ara
        User user = userDao.findByGoogleId(googleId);

        if (user != null) {
            // Mevcut Google kullanıcısı — profil bilgilerini güncelle
            user.setProfilePictureUrl(pictureUrl);
            user.setUsername(name);
            userDao.update(user);
            logger.info("Google kullanıcısı giriş yaptı: id={}, googleId={}", user.getId(), googleId);
            return user;
        }

        // E-posta ile kontrol et (LOCAL hesap var mı?)
        User existingByEmail = userDao.findByEmail(email);
        if (existingByEmail != null) {
            // Mevcut LOCAL hesabı Google ile bağla
            existingByEmail.setGoogleId(googleId);
            existingByEmail.setAuthProvider("GOOGLE");
            existingByEmail.setEmailVerified(true);
            existingByEmail.setProfilePictureUrl(pictureUrl);
            existingByEmail.setVerificationToken(null);
            existingByEmail.setVerificationTokenExpiry(null);
            userDao.update(existingByEmail);
            logger.info("Mevcut hesap Google ile bağlandı: id={}, email={}", existingByEmail.getId(), email);
            return existingByEmail;
        }

        // Yeni Google kullanıcısı oluştur
        user = new User();
        user.setUsername(name);
        user.setEmail(email);
        user.setGoogleId(googleId);
        user.setAuthProvider("GOOGLE");
        user.setEmailVerified(true); // Google zaten doğrulamış
        user.setProfilePictureUrl(pictureUrl);

        userDao.save(user);
        logger.info("Yeni Google kullanıcısı kaydedildi: googleId={}, email={}", googleId, email);
        return user;
    }

    /**
     * E-posta doğrulama token'ını doğrular.
     */
    @Transactional
    public boolean verifyEmail(String token) {
        User user = userDao.findByVerificationToken(token);

        if (user == null) {
            logger.warn("Geçersiz doğrulama token'ı: {}", token);
            return false;
        }

        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            logger.warn("Doğrulama token'ı süresi dolmuş: userId={}", user.getId());
            return false;
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userDao.update(user);

        logger.info("E-posta doğrulandı: userId={}, email={}", user.getId(), user.getEmail());
        return true;
    }

    /**
     * Doğrulama mailini tekrar gönderir.
     */
    @Transactional
    public void resendVerificationEmail(String email, String lang) {
        User user = userDao.findByEmail(email);

        if (user == null) {
            throw new IllegalArgumentException("USER_NOT_FOUND");
        }

        if (user.isEmailVerified()) {
            throw new IllegalArgumentException("ALREADY_VERIFIED");
        }

        // Yeni token oluştur
        String newToken = UUID.randomUUID().toString();
        user.setVerificationToken(newToken);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(VERIFICATION_TOKEN_HOURS));
        userDao.update(user);

        emailService.sendVerificationEmail(email, user.getUsername(), newToken, lang);
        logger.info("Doğrulama maili tekrar gönderildi: email={}", email);
    }
}
