package com.stemsep.service;

import com.stemsep.dao.UserDao;
import com.stemsep.exception.EmailExistsException;
import com.stemsep.exception.EmailNotVerifiedException;
import com.stemsep.exception.InvalidCredentialsException;
import com.stemsep.exception.InvalidTokenException;
import com.stemsep.exception.UserNotFoundException;
import com.stemsep.exception.UsernameExistsException;
import com.stemsep.model.User;
import com.stemsep.util.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final int VERIFICATION_TOKEN_HOURS = 24;
    private static final int RESET_TOKEN_HOURS = 1;

    @Autowired
    private UserDao userDao;

    @Autowired
    private EmailService emailService;

    @Transactional
    public User registerLocal(String username, String email, String password, String lang) {
        if (userDao.findByUsername(username) != null) {
            throw new UsernameExistsException(username);
        }
        if (userDao.findByEmail(email) != null) {
            throw new EmailExistsException(email);
        }

        String token = UUID.randomUUID().toString();
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(PasswordHasher.sha256(password));
        user.setAuthProvider("LOCAL");
        user.setEmailVerified(false);
        user.setVerificationToken(token);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(VERIFICATION_TOKEN_HOURS));

        userDao.save(user);
        logger.info("Yeni kullanıcı kaydedildi: username={}, provider=LOCAL", username);

        try {
            emailService.sendVerificationEmail(email, username, token, lang);
        } catch (Exception e) {
            logger.error("Doğrulama maili gönderilemedi: email={}", email, e);
        }
        return user;
    }

    @Transactional(readOnly = true)
    public User loginLocal(String email, String password) {
        User user = userDao.findByEmail(email);
        if (user == null || !"LOCAL".equals(user.getAuthProvider())) {
            throw new InvalidCredentialsException(email);
        }
        if (!PasswordHasher.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException(email);
        }
        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException(email);
        }
        logger.info("Kullanıcı giriş yaptı: id={}, username={}", user.getId(), user.getUsername());
        return user;
    }

    @Transactional
    public User loginOrRegisterGoogle(String googleId, String email, String name, String pictureUrl) {
        User user = userDao.findByGoogleId(googleId);
        if (user != null) {
            user.setProfilePictureUrl(pictureUrl);
            user.setUsername(name);
            userDao.update(user);
            return user;
        }

        User existing = userDao.findByEmail(email);
        if (existing != null) {
            existing.setGoogleId(googleId);
            existing.setAuthProvider("GOOGLE");
            existing.setEmailVerified(true);
            existing.setProfilePictureUrl(pictureUrl);
            existing.setVerificationToken(null);
            existing.setVerificationTokenExpiry(null);
            userDao.update(existing);
            return existing;
        }

        user = new User();
        user.setUsername(generateUniqueUsername(name));
        user.setEmail(email);
        user.setGoogleId(googleId);
        user.setAuthProvider("GOOGLE");
        user.setEmailVerified(true);
        user.setProfilePictureUrl(pictureUrl);
        userDao.save(user);
        logger.info("Yeni Google kullanıcısı kaydedildi: googleId={}, email={}", googleId, email);
        return user;
    }

    private String generateUniqueUsername(String base) {
        if (userDao.findByUsername(base) == null) {
            return base;
        }
        for (int i = 2; i < 1000; i++) {
            String candidate = base + i;
            if (userDao.findByUsername(candidate) == null) {
                return candidate;
            }
        }
        return base + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userDao.findByVerificationToken(token);
        if (user == null) {
            throw new InvalidTokenException(token);
        }
        if (user.getVerificationTokenExpiry() == null
                || user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new com.stemsep.exception.VerificationTokenExpiredException(user.getEmail());
        }
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userDao.update(user);
        logger.info("E-posta doğrulandı: userId={}", user.getId());
    }

    @Transactional
    public void requestPasswordReset(String email, String lang) {
        User user = userDao.findByEmail(email);
        if (user == null || !"LOCAL".equals(user.getAuthProvider())) {
            logger.info("Parola sıfırlama: kullanıcı bulunamadı veya LOCAL değil, sessiz geçildi (email={})", email);
            return;
        }
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(RESET_TOKEN_HOURS));
        userDao.update(user);
        try {
            emailService.sendPasswordResetEmail(email, user.getUsername(), token, lang);
            logger.info("Parola sıfırlama maili gönderildi: userId={}", user.getId());
        } catch (Exception e) {
            logger.error("Parola sıfırlama maili gönderilemedi: email={}", email, e);
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userDao.findByResetToken(token);
        if (user == null || user.getResetTokenExpiry() == null
                || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException(token);
        }
        user.setPasswordHash(PasswordHasher.sha256(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userDao.update(user);
        logger.info("Parola sıfırlandı: userId={}", user.getId());
    }

    @Transactional
    public void resendVerificationEmail(String email, String lang) {
        User user = userDao.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException(email);
        }
        if (user.isEmailVerified()) {
            return;
        }
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(VERIFICATION_TOKEN_HOURS));
        userDao.update(user);
        emailService.sendVerificationEmail(email, user.getUsername(), token, lang);
    }
}
