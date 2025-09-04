package com.andreiromila.vetl.factories;

import com.andreiromila.vetl.role.Role;
import com.andreiromila.vetl.user.User;
import com.github.javafaker.Faker;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.IntStream;

public class AggregatesFactory {

    static final Instant now = Instant.now();

    static Faker faker = Faker.instance(Locale.of("es", "ES"));

    public static Role getAdminRole() {
        return new Role(1L, "Admin", "", Instant.now(), Instant.now());
    }

    public static Role getEditorRole() {
        return new Role(2L, "Editor", "", Instant.now(), Instant.now());
    }

    public static Role getViewerRole() {
        return new Role(2L, "Viewer", "", Instant.now(), Instant.now());
    }

    public static User createAdmin(String username) {

        var user = new User();

        user.setUsername(username);
        user.setEmail(faker.internet().safeEmailAddress());
        user.setPassword(faker.internet().password());
        user.setFullName(faker.name().fullName());
        user.setEnabled(true);
        user.setModifiedAt(now);

        user.setRoles(Set.of(getAdminRole()));

        return user;
    }

    public static User createUser(String username) {

        var user = new User();

        user.setUsername(username);
        user.setEmail(faker.internet().safeEmailAddress());
        user.setPassword(faker.internet().password());
        user.setFullName(faker.name().fullName());

        final String avatarUrl = faker.avatar().image();
        user.setAvatarKey(avatarUrl.substring(avatarUrl.lastIndexOf('/') + 1));
        user.setAvatarUrl(avatarUrl);

        user.setEnabled(true);
        user.setModifiedAt(now);

        user.setRoles(Set.of(getViewerRole()));

        return user;
    }

    public static User createUser() {
        return createUser(faker.name().username());
    }

    public static List<User> createUsers(int total) {
        return IntStream.rangeClosed(1, total)
                .mapToObj(index -> createUser(faker.name().username()))
                .toList();
    }

}
