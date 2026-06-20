package org.example.finalproject;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.example.finalproject.exceptions.MailerException;

import java.util.Properties;

public class Mailer {

    private static final String fromEmail = "socnetjavafx@gmail.com";
    private static final String appPassword = "fuzo idmu mvbw sxqg";

    private static Session session = null;

    protected Mailer() {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, appPassword);
            }
        });

    }

    public static void sendEmail(String recipient, String message_title, String message_content) throws MessagingException {
        if(session == null) {
            throw new MailerException("Mailer not initialized [Mailer()]");
        }
        if(recipient.isEmpty() || message_title.isEmpty() || message_content.isEmpty()) {
            throw new MailerException("Fill all arguments!");
        }

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("mgiorgi685@gmail.com"));
        message.setSubject(message_title);
        message.setContent(message_content, "text/html");
        Transport.send(message);
    }

    public static boolean isEmail(String text) {
        if(text != null && text.contains("@") && text.contains(".") && text.length() > 5) return true;
        return false;
    }
}
