package com.andreiromila.vetl.mail;

import com.andreiromila.vetl.user.User;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class EmailService {

    /**
     * The "From" address, injected from application properties.
     */
    private final String fromEmail;

    /**
     * The public URL for the logo, injected from application properties.
     */
    private final String logoUrl;

    /**
     * The {@link Configuration} bean for FreeMarker template processing.
     */
    private final Configuration freemarkerConfig;

    /**
     * The {@link JavaMailSender} bean for sending emails.
     */
    private final JavaMailSender emailSender;

    /**
     * Constructs the EmailService with required beans and properties.
     *
     * @param emailSender      The {@link JavaMailSender} bean for sending emails.
     * @param freemarkerConfig The {@link Configuration} bean for FreeMarker template processing.
     * @param from             The "From" address, injected from application properties.
     * @param logoUrl          The public URL for the logo, injected from application properties.
     */
    public EmailService(JavaMailSender emailSender,
                        Configuration freemarkerConfig,
                        @Value("${spring.mail.properties.mail.from}") String from,
                        @Value("${spring.mail.properties.mail.logo-url}") String logoUrl) {

        this.emailSender = emailSender;
        this.freemarkerConfig = freemarkerConfig;

        this.fromEmail = from;
        this.logoUrl = logoUrl;
    }

    /**
     * Composes and sends an account activation email to a new user.
     *
     * @param user           The {@link User} object representing the recipient.
     * @param activationLink The unique URL the user will click to activate their account.
     */
    public void sendActivationEmail(User user, String activationLink) {

        try {

            final Template template = freemarkerConfig.getTemplate("emails/activation-mail.ftl");

            // Construye el modelo de datos para la plantilla
            final Map<String, Object> model = new HashMap<>();
            model.put("fullName", user.getFullName());
            model.put("activationLink", activationLink);
            model.put("logoUrl", logoUrl);

            final String htmlBody = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

            final MimeMessage message = emailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());

            // Todo: Extract this to the configuration file
            helper.setSubject("Hey %s Complete Your Vortex ETL Registration".formatted(user.getFullName()));
            helper.setText(htmlBody, true);

            emailSender.send(message);

            log.info("HTML activation email sent successfully to {}", user.getEmail());

        } catch (Exception e) {
            log.error("Failed to send HTML activation email to {}: {}", user.getEmail(), e.getMessage(), e);
        }
    }
}
