package ke.co.skyworld.internship.util.infra;

import jakarta.activation.CommandMap;
import jakarta.activation.MailcapCommandMap;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import ke.co.skyworld.internship.config.Constants;
import ke.co.skyworld.internship.util.formatting.TemplateEngine;
import ke.co.skyworld.internship.util.logging.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * sky-core (ke.co.skyworld.internship.skycore.service)
 * Created by: oloo
 * On: 8/12/26. 5:43 PM
 * Description:
 **/

public class EmailService {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);
    private static final Properties PROPS = buildSmtpProperties();

    static {
        /*suggested FIX - adding this block after issues in productions
         * Refrence : https://stackoverflow.com/questions/21856211/javax-activation-unsupporteddatatypeexception-no-object-dch-for-mime-type-multi
         *
         *  */
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);
    }

    public static boolean validateConfig() {
        if (!Constants.isEmailEnabled()) {
            Log.info(EmailService.class, "validateConfig", "Email is disabled - skipping config check.");
            return true;
        }

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        String host = Constants.getEmailHost();
        int port = Constants.getEmailPort();
        boolean startTls = Constants.isEmailStartTls();
        boolean sslPort = (port == 465);
        boolean tlsPort = (port == 587 || port == 25);

        if (host.isBlank()) errors.add("email.host is missing");
        if (port <= 0 || port > 65535) errors.add("email.port is invalid: " + port);
        else if (sslPort && startTls)
            warnings.add("port=465 uses implicit SSL but starttls=true - starttls will be ignored");
        else if (tlsPort && !startTls) warnings.add("port=" + port + " normally requires STARTTLS but starttls=false");
        else if (!sslPort && !tlsPort) warnings.add("port=" + port + " is non-standard (expected 25/465/587)");

        String user = Constants.getEmailUser();
        String pass = Constants.getEmailPass();
        String from = Constants.getEmailFrom();

        if (user == null || user.isBlank()) errors.add("email.username is missing");
        if (pass == null || pass.isBlank()) errors.add("email.password is missing");
        if (from == null || from.isBlank()) {
            errors.add("email.from is missing");
        } else {
            try {
                new InternetAddress(from, true).validate();
            } catch (Exception e) {
                errors.add("email.from is not valid: \"" + from + "\" - " + e.getMessage());
            }
        }

        warnings.forEach(w -> Log.warning(EmailService.class, "validateConfig", " >>> " + w));
        errors.forEach(e -> Log.error(EmailService.class, "validateConfig", " >>> " + e, null));

        if (!errors.isEmpty()) {
            Log.error(EmailService.class, "validateConfig",
                    errors.size() + " email misconfiguration(s) - emails will fail until resolved.", null);
            return false;
        }

        String protocol = sslPort ? "SSL/TLS" : (startTls ? "STARTTLS" : "plaintext");
        Log.info(EmailService.class, "validateConfig",
                "Email config OK - host=" + host + " port=" + port
                        + " [" + protocol + "] user=" + user + " from=" + from);
        return true;
    }

    private static Properties buildSmtpProperties() {
        int port = Constants.getEmailPort();
        boolean sslPort = (port == 465);

        Properties p = new Properties();
        p.put("mail.smtp.host", Constants.getEmailHost());
        p.put("mail.smtp.port", String.valueOf(port));
        p.put("mail.smtp.auth", "true");

        if (sslPort) {
            p.put("mail.smtp.ssl.enable", "true");
            p.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
            p.put("mail.smtp.socketFactory.port", String.valueOf(port));
            p.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            p.put("mail.smtp.socketFactory.fallback", "false");
        } else {
            p.put("mail.smtp.starttls.enable", String.valueOf(Constants.isEmailStartTls()));
            p.put("mail.smtp.starttls.required", String.valueOf(Constants.isEmailStartTls()));
        }
        return p;
    }

    public void sendVerificationOtp(String toEmail, String name, String otp) {
        Map<String, Object> model = Map.of(
                "name", name,
                "otpCode", otp,
                "otpExpiryMinutes", Constants.getOtpTtlMinutes()
        );
        sendAsync(toEmail, "Verify your Sky-Core account", "templates/mail/verification-otp.ftl", model);
    }

    public void sendLoginMfaOtp(String toEmail, String name, String otp) {
        Map<String, Object> model = Map.of(
                "name", name,
                "otpCode", otp,
                "otpExpiryMinutes", Constants.getOtpTtlMinutes()
        );
        sendAsync(toEmail, "Your Sky-Core sign-in code", "templates/mail/login-mfa-otp.ftl", model);
    }

    public void sendPasswordReset(String toEmail, String name, String resetLink) {
        Map<String, Object> model = Map.of(
                "name", name,
                "resetLink", resetLink,
                "expiryMinutes", Constants.getPasswordResetTtlMinutes()
        );
        sendAsync(toEmail, "Reset your Sky-Core password", "templates/mail/password-reset.ftl", model);
    }

    public void sendWelcome(String toEmail, String name) {
        Map<String, Object> model = Map.of("name", name);
        sendAsync(toEmail, "Welcome to Sky-Core", "templates/mail/welcome.ftl", model);
    }

    public void sendKycApproved(String toEmail, String name) {
        Map<String, Object> model = Map.of("name", name);
        sendAsync(toEmail, "Your Sky-Core account is verified", "templates/mail/kyc-approved.ftl", model);
    }

    public void sendKycRejected(String toEmail, String name, String reason) {
        Map<String, Object> model = Map.of("name", name, "reason", reason);
        sendAsync(toEmail, "Action needed on your Sky-Core verification", "templates/mail/kyc-rejected.ftl", model);
    }

    public void sendWithAttachment(String to, String subject, String htmlBody,
                                   byte[] attachmentData, String attachmentName) {
        if (!Constants.isEmailEnabled()) {
            Log.info(this.getClass(), "sendWithAttachment",
                    "Email disabled - skipping send to " + to);
            return;
        }
        EXECUTOR.submit(() -> {
            try {
                doSendWithAttachment(to, subject, htmlBody, attachmentData, attachmentName);
            } catch (Exception e) {
                Log.error(EmailService.class, "sendWithAttachment",
                        "Failed to send email to " + to + ": " + e.getMessage(), e);
            }
        });
    }

    private void sendAsync(String to, String subject, String templateName,
                           Map<String, Object> model) {
        if (!Constants.isEmailEnabled()) {
            Log.info(EmailService.class, "sendAsync",
                    "Email disabled - skipping send to " + to + " (template=" + templateName + ")");
            return;
        }
        EXECUTOR.submit(() -> {
            try {
                String html = TemplateEngine.render(templateName, model);
                doSend(to, subject, html);
            } catch (Exception e) {
                Log.error(EmailService.class, "sendAsync",
                        "Failed to send email to " + to + ": " + e.getMessage(), e);
            }
        });
    }

    public void doSend(String to, String subject, String htmlBody) throws Exception {
        Session session = buildSession();

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(Constants.getEmailFrom()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlBody, "text/html; charset=UTF-8");
        Multipart multipart = new MimeMultipart("alternative");
        multipart.addBodyPart(htmlPart);
        message.setContent(multipart);

        Transport.send(message);
        Log.info(EmailService.class, "doSend", "Email sent to " + to + ": " + subject);
    }

    private void doSendWithAttachment(String to, String subject, String htmlBody,
                                      byte[] attachmentData, String attachmentName) throws Exception {
        Session session = buildSession();

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(Constants.getEmailFrom()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        Multipart multipart = new MimeMultipart();

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlBody, "text/html; charset=UTF-8");
        multipart.addBodyPart(htmlPart);

        if (attachmentData != null && attachmentData.length > 0) {
            MimeBodyPart attachPart = new MimeBodyPart();
            attachPart.setFileName(attachmentName);
            attachPart.setContent(attachmentData, "application/pdf");
            attachPart.setDisposition(MimeBodyPart.ATTACHMENT);
            multipart.addBodyPart(attachPart);
        }

        message.setContent(multipart);
        Transport.send(message);
        Log.info(this.getClass(), "doSendWithAttachment",
                "Email with attachment sent to " + to + ": " + subject);
    }

    private Session buildSession() {
        String user = Constants.getEmailUser();
        String pass = Constants.getEmailPass();
        return Session.getInstance(PROPS, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });
    }

}

