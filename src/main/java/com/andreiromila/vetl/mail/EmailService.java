package com.andreiromila.vetl.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    private final String fromEmail;

    private final JavaMailSender emailSender;

    public EmailService(JavaMailSender emailSender, @Value("${spring.mail.properties.mail.from}") String from) {
        this.fromEmail = from;
        this.emailSender = emailSender;
    }

    /**
     * Sends a simple plain text email.
     *
     * @param to The recipient's email address.
     * @param subject The subject of the email.
     * @param text The body of the email.
     */
    public void sendSimpleMessage(String to, String subject, String text) {
        try {

            final SimpleMailMessage message = new SimpleMailMessage();

            // Todo: We must configure this with something more professional
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            // Send the email to the user
            emailSender.send(message);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
