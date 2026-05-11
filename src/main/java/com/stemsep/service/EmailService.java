package com.stemsep.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private Environment env;

    public void sendVerificationEmail(String to, String username, String token, String lang) {
        boolean isTr = "tr".equals(lang);
        String subject = isTr ? "Splitnorder - E-posta Doğrulama" : "Splitnorder - Email Verification";
        String url = env.getProperty("APP_BASE_URL", "http://localhost:8090") + "/auth/verify-email?token=" + token;
        String body = isTr
                ? "Merhaba " + username + ",\n\nE-postanı doğrulamak için: " + url + "\n\nBu link 24 saat geçerlidir."
                : "Hello " + username + ",\n\nVerify your email: " + url + "\n\nThis link is valid for 24 hours.";
        send(to, subject, body);
    }

    private void send(String to, String subject, String body) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", env.getProperty("SMTP_HOST"));
            props.put("mail.smtp.port", env.getProperty("SMTP_PORT"));
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.socketFactory.port", env.getProperty("SMTP_PORT"));
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(env.getProperty("SMTP_USER"), env.getProperty("SMTP_PASSWORD"));
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(env.getProperty("EMAIL_FROM"), env.getProperty("SMTP_FROM_NAME")));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject, "UTF-8");
            message.setText(body, "UTF-8");

            Transport.send(message);
            logger.info("E-posta gönderildi: to={}", to);
        } catch (Exception e) {
            logger.error("E-posta gönderimi başarısız: to={}", to, e);
            throw new RuntimeException("E-posta gönderilemedi", e);
        }
    }
}
