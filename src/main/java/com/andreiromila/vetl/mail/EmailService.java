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

    private final String fromEmail;
    private final String logoUrl;

    private final Configuration freemarkerConfig;
    private final JavaMailSender emailSender;

    public EmailService(JavaMailSender emailSender,
                        Configuration freemarkerConfig,
                        @Value("${spring.mail.properties.mail.from}") String from,
                        @Value("${spring.mail.properties.mail.logo-url}") String logoUrl) {

        this.emailSender = emailSender;
        this.freemarkerConfig = freemarkerConfig;

        this.fromEmail = from;
        this.logoUrl = logoUrl;
    }

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

            helper.setFrom(fromEmail); //  #5046E5 - #5039F6
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
