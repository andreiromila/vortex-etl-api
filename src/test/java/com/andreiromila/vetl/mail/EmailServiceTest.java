package com.andreiromila.vetl.mail;

import com.andreiromila.vetl.user.User;
import freemarker.template.Configuration;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    JavaMailSender mockEmailSender;

    @Mock
    MimeMessage mockMimeMessage;

    Configuration freemarkerConfig;
    EmailService emailService;

    // Test data
    final String FROM_EMAIL = "test@vortex.com";
    final String LOGO_URL = "http://example.com/logo.png";

    @BeforeEach
    void setUp() {

        freemarkerConfig = new Configuration(Configuration.VERSION_2_3_34);
        freemarkerConfig.setClassForTemplateLoading(this.getClass(), "/templates/");
        freemarkerConfig.setDefaultEncoding("UTF-8");

        // Instantiate the service with our mocks
        emailService = new EmailService(mockEmailSender, freemarkerConfig, FROM_EMAIL, LOGO_URL);
    }

    @Test
    void sendActivationEmail_composesAndSendsMessageCorrectly() throws MessagingException {
        // Given
        User testUser = new User();
        testUser.setEmail("test.user@example.com");
        testUser.setFullName("Test User FullName");
        String activationLink = "http://app.com/activate?token=1234";

        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(mockEmailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.sendActivationEmail(testUser, activationLink);

        // Then
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mockEmailSender).send(captor.capture());

        MimeMessage sentMessage = captor.getValue();
        assertThat(sentMessage.getSubject()).isEqualTo("Hey Test User FullName Complete Your Vortex ETL Registration");
        assertThat(sentMessage.getRecipients(MimeMessage.RecipientType.TO)[0].toString()).isEqualTo("test.user@example.com");
        assertThat(sentMessage.getFrom()[0].toString()).isEqualTo(FROM_EMAIL);
    }

    @Test
    void sendActivationEmail_whenTemplateIsNotFound_doesNotSendEmail() {
        // Given
        User testUser = new User();
        testUser.setEmail("test.user@example.com");

        // Esta vez, en lugar de mockear, podemos hacer que falle de verdad,
        // simplemente pidiendo una plantilla que no existe.
        freemarkerConfig.setTemplateLoader(null); // Desactivamos el cargador de plantillas para forzar un error.

        // When
        emailService.sendActivationEmail(testUser, "some-link");

        // Then
        verify(mockEmailSender, never()).send(any(MimeMessage.class));
    }

}