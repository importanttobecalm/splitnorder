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
        String subject = isTr ? "Splitnorder · E-posta Doğrulama" : "Splitnorder · Email Verification";
        String url = env.getProperty("APP_BASE_URL", "http://localhost:8090") + "/auth/verify-email?token=" + token;
        String html = buildVerificationHtml(username, url, isTr);
        send(to, subject, html);
    }

    /**
     * Müşterilerin geniş çoğunluğunda (Gmail, Outlook web/desktop, Apple Mail)
     * tutarlı görünmesi için: table-based layout, inline CSS, web font yok
     * (system stack), tek dış link.
     */
    private String buildVerificationHtml(String username, String url, boolean isTr) {
        String title       = isTr ? "E-postanı doğrula" : "Verify your email";
        String greeting    = isTr ? ("Merhaba " + username + ",") : ("Hello " + username + ",");
        String intro       = isTr
                ? "Splitnorder hesabını kullanmaya başlamak için e-posta adresini doğrulaman gerekiyor. Aşağıdaki düğmeye tıklamak yeterli."
                : "To start using your Splitnorder account, please confirm your email address by clicking the button below.";
        String cta         = isTr ? "E-postamı doğrula" : "Verify my email";
        String fallback    = isTr ? "Düğme çalışmazsa bu bağlantıyı tarayıcına yapıştır:" : "If the button doesn't work, paste this link into your browser:";
        String expiryNote  = isTr ? "Bu bağlantı 24 saat boyunca geçerlidir." : "This link is valid for 24 hours.";
        String ignoreNote  = isTr
                ? "Bu kaydı sen yapmadıysan bu e-postayı yok sayabilirsin."
                : "If you didn't create this account, you can safely ignore this email.";
        String footer      = isTr
                ? "Splitnorder · BM470 İleri Java Programlama Projesi · Düzce Üniversitesi"
                : "Splitnorder · BM470 Advanced Java Programming Project · Düzce University";
        String tagline     = isTr ? "Müziğini katmanlarına ayır." : "Split your music into stems.";

        return ""
            + "<!DOCTYPE html>\n"
            + "<html lang=\"" + (isTr ? "tr" : "en") + "\">\n"
            + "<head>\n"
            + "  <meta charset=\"UTF-8\">\n"
            + "  <meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">\n"
            + "  <title>" + title + "</title>\n"
            + "</head>\n"
            + "<body style=\"margin:0;padding:0;background-color:#EAF2FB;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#1E3A5F;\">\n"
            + "  <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#EAF2FB;padding:32px 16px;\">\n"
            + "    <tr><td align=\"center\">\n"
            + "      <table role=\"presentation\" width=\"560\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"max-width:560px;width:100%;background-color:#ffffff;border-radius:16px;box-shadow:0 8px 24px rgba(74,144,226,0.12);overflow:hidden;\">\n"
            + "        <tr><td style=\"padding:32px 40px 16px 40px;\">\n"
            + "          <div style=\"font-size:13px;letter-spacing:0.08em;text-transform:uppercase;color:#4A90E2;font-weight:600;\">Splitnorder</div>\n"
            + "          <div style=\"font-size:13px;color:#6B7C93;margin-top:4px;\">" + tagline + "</div>\n"
            + "        </td></tr>\n"
            + "        <tr><td style=\"padding:8px 40px 0 40px;\">\n"
            + "          <h1 style=\"margin:0 0 12px 0;font-size:24px;font-weight:700;color:#1E3A5F;line-height:1.3;\">" + title + "</h1>\n"
            + "          <p style=\"margin:0 0 8px 0;font-size:16px;line-height:1.6;color:#1E3A5F;\">" + greeting + "</p>\n"
            + "          <p style=\"margin:0 0 24px 0;font-size:15px;line-height:1.6;color:#6B7C93;\">" + intro + "</p>\n"
            + "        </td></tr>\n"
            + "        <tr><td align=\"center\" style=\"padding:0 40px 24px 40px;\">\n"
            + "          <a href=\"" + url + "\" style=\"display:inline-block;background-color:#4A90E2;color:#ffffff;text-decoration:none;font-weight:600;font-size:15px;padding:14px 32px;border-radius:12px;\">" + cta + "</a>\n"
            + "        </td></tr>\n"
            + "        <tr><td style=\"padding:0 40px 16px 40px;\">\n"
            + "          <p style=\"margin:0 0 6px 0;font-size:13px;line-height:1.5;color:#6B7C93;\">" + fallback + "</p>\n"
            + "          <p style=\"margin:0 0 20px 0;font-size:13px;line-height:1.5;word-break:break-all;\"><a href=\"" + url + "\" style=\"color:#4A90E2;text-decoration:none;\">" + url + "</a></p>\n"
            + "          <div style=\"height:1px;background-color:#EAF2FB;margin:8px 0 16px 0;\"></div>\n"
            + "          <p style=\"margin:0 0 6px 0;font-size:13px;line-height:1.5;color:#6B7C93;\">⏱️ " + expiryNote + "</p>\n"
            + "          <p style=\"margin:0;font-size:13px;line-height:1.5;color:#A4B0C0;\">" + ignoreNote + "</p>\n"
            + "        </td></tr>\n"
            + "        <tr><td style=\"padding:24px 40px;background-color:#F7FAFD;border-top:1px solid #EAF2FB;\">\n"
            + "          <p style=\"margin:0;font-size:12px;color:#A4B0C0;text-align:center;\">" + footer + "</p>\n"
            + "        </td></tr>\n"
            + "      </table>\n"
            + "    </td></tr>\n"
            + "  </table>\n"
            + "</body>\n"
            + "</html>";
    }

    private void send(String to, String subject, String htmlBody) {
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
            message.setContent(htmlBody, "text/html; charset=UTF-8");

            Transport.send(message);
            logger.info("E-posta gönderildi: to={}", to);
        } catch (Exception e) {
            logger.error("E-posta gönderimi başarısız: to={}", to, e);
            throw new RuntimeException("E-posta gönderilemedi", e);
        }
    }
}
