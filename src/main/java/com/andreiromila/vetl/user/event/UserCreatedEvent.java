package com.andreiromila.vetl.user.event;

import com.andreiromila.vetl.user.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a new user is created.
 * Contains the user object that was created.
 */
@Getter
public class UserCreatedEvent extends ApplicationEvent {

    private final User user;

    public UserCreatedEvent(Object source, User user) {
        super(source);
        this.user = user;
    }
}

