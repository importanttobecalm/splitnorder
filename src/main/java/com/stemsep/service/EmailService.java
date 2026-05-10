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

/**
 * SMTP e-posta gönderme servisi.
 * hibernate.properties'teki SMTP_* anahtarlarını kullanır.
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private Environment env;

    /**
     * HTML formatlı e-posta gönderir.
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            String host = env.getProperty("SMTP_HOST");
            String port = env.getProperty("SMTP_PORT");
            String user = env.getProperty("SMTP_USER");
            String password = env.getProperty("SMTP_PASSWORD");
            String fromEmail = env.getProperty("EMAIL_FROM");
            String fromName = env.getProperty("SMTP_FROM_NAME");

            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.socketFactory.port", port);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, password);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, fromName));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject, "UTF-8");
            message.setContent(htmlContent, "text/html; charset=UTF-8");

            Transport.send(message);
            logger.info("E-posta başarıyla gönderildi: to={}", to);

        } catch (Exception e) {
            logger.error("E-posta gönderimi başarısız: to={}, error={}", to, e.getMessage(), e);
            throw new RuntimeException("E-posta gönderilemedi", e);
        }
    }

    /**
     * E-posta doğrulama maili gönderir.
     */
    public void sendVerificationEmail(String to, String username, String token, String lang) {
        String baseUrl = "http://localhost:5173"; // React dev server
        String verifyUrl = baseUrl + "/verify-email?token=" + token;

        String subject;
        String htmlContent;

        if ("tr".equals(lang)) {
            subject = "Splitnorder - E-posta Doğrulama";
            htmlContent = buildVerificationEmailHtml(username, verifyUrl, "tr");
        } else {
            subject = "Splitnorder - Email Verification";
            htmlContent = buildVerificationEmailHtml(username, verifyUrl, "en");
        }

        sendHtmlEmail(to, subject, htmlContent);
    }

    private String buildVerificationEmailHtml(String username, String verifyUrl, String lang) {
        boolean isTr = "tr".equals(lang);

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #0f0f23; color: #e0e0e0; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 0 auto; padding: 40px 20px; }
                    .card { background: linear-gradient(135deg, #1a1a3e 0%%, #2d1b69 100%%); border-radius: 16px; padding: 40px; text-align: center; }
                    .logo { font-size: 28px; font-weight: 700; color: #a78bfa; margin-bottom: 8px; }
                    .logo-icon { font-size: 32px; }
                    h1 { color: #f0f0f0; font-size: 24px; margin: 20px 0 10px; }
                    p { color: #b0b0d0; font-size: 16px; line-height: 1.6; }
                    .btn { display: inline-block; background: linear-gradient(135deg, #8b5cf6, #6d28d9); color: #ffffff !important;
                           padding: 14px 40px; border-radius: 12px; text-decoration: none; font-size: 16px; font-weight: 600;
                           margin: 24px 0; transition: all 0.3s; }
                    .btn:hover { background: linear-gradient(135deg, #7c3aed, #5b21b6); }
                    .footer { margin-top: 30px; font-size: 13px; color: #666; }
                    .divider { height: 1px; background: linear-gradient(to right, transparent, #4a3a8a, transparent); margin: 24px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="card">
                        <div class="logo-icon">🎵</div>
                        <div class="logo">Splitnorder</div>
                        <h1>%s</h1>
                        <div class="divider"></div>
                        <p>%s <strong>%s</strong>!</p>
                        <p>%s</p>
                        <a href="%s" class="btn">%s</a>
                        <p class="footer">%s</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                isTr ? "E-posta Doğrulama" : "Email Verification",
                isTr ? "Merhaba" : "Hello",
                username,
                isTr ? "Hesabınızı doğrulamak için aşağıdaki butona tıklayın:" : "Click the button below to verify your account:",
                verifyUrl,
                isTr ? "E-postamı Doğrula" : "Verify My Email",
                isTr ? "Bu link 24 saat geçerlidir. Bu maili siz istemediyseniz lütfen göz ardı edin." :
                       "This link is valid for 24 hours. If you did not request this email, please ignore it."
        );
    }
}
