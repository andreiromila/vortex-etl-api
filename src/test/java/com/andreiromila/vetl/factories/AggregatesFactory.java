package com.andreiromila.vetl.factories;

import com.andreiromila.vetl.user.User;
import com.github.javafaker.Faker;

import java.time.Instant;
import java.util.Locale;

public class AggregatesFactory {

    static final Instant now = Instant.now();

    static Faker faker = Faker.instance(Locale.of("es", "ES"));

    public static User createUser(String username) {

        var user = new User();

        user.setUsername(username);
        user.setEmail(faker.internet().safeEmailAddress());
        user.setPassword(faker.internet().password());
        user.setFullName(faker.name().fullName());
        user.setEnabled(true);
        user.setModifiedAt(now);

        return user;
    }

    public static User createUser() {
        return createUser(faker.name().username());
    }

}
