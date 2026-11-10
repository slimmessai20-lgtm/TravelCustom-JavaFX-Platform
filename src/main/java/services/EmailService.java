package services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.util.Objects;
import java.util.Properties;

/**
 * Envoi d'emails via Gmail SMTP (STARTTLS).
 *
 * Note: pour Gmail, il faut utiliser un "App Password" (mot de passe d'application).
 */
public class EmailService {

    private final String smtpHost;
    private final int smtpPort;
    private final String username;
    private final String appPassword;

    public EmailService(String username, String appPassword) {
        this("smtp.gmail.com", 587, username, appPassword);
    }

    public EmailService(String smtpHost, int smtpPort, String username, String appPassword) {
        this.smtpHost = Objects.requireNonNull(smtpHost);
        this.smtpPort = smtpPort;
        this.username = Objects.requireNonNull(username);
        this.appPassword = Objects.requireNonNull(appPassword);
    }

    public void sendText(String to, String subject, String body) {
        send(to, subject, body, null);
    }

    public void send(String to, String subject, String body, Attachment attachment) {
        try {
            Session session = Session.getInstance(buildProps(), new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, appPassword);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            if (attachment == null) {
                message.setText(body);
            } else {
                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(body);

                MimeBodyPart filePart = new MimeBodyPart();
                filePart.setFileName(attachment.fileName());
                filePart.setContent(attachment.bytes(), attachment.contentType());

                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(textPart);
                multipart.addBodyPart(filePart);
                message.setContent(multipart);
            }

            Transport.send(message);
        } catch (Exception e) {
            // On ne crash pas l'application UI si l'email échoue.
            System.err.println("[EmailService] Échec envoi email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Properties buildProps() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", String.valueOf(smtpPort));
        return props;
    }

    public record Attachment(String fileName, String contentType, byte[] bytes) {}
}
