package com.andreiromila.vetl.user.event;

import com.andreiromila.vetl.mail.EmailService;
import com.andreiromila.vetl.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listens for user-related application events and triggers actions.
 */
@Component
@Slf4j
public class UserEventListener {

    private final EmailService emailService;

    public UserEventListener(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Handles the UserCreatedEvent by sending an activation email.
     *
     * @param event The event containing the newly created user.
     */
    @EventListener
    public void handleUserCreatedEvent(UserCreatedEvent event) {

        log.info("Handling UserCreatedEvent for username: {}", event.getUser().getUsername());

        final User user = event.getUser();
        // Construye el enlace de invitación

        // TODO: Externalizar la URL base del frontend a application.yml
        final String activationLink = "http://localhost:5173/set-password?username="
                + user.getUsername()
                + "&token="
                + user.getEmailActivationCode();

        emailService.sendActivationEmail(user, activationLink);
    }

}
