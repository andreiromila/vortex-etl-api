package com.andreiromila.vetl.user.web;

import com.andreiromila.vetl.user.User;

import java.time.Instant;

public record UserCreateResponse(
        Long id,
        String username,
        String email,
        String fullName,
        Instant createdAt,
        Instant modifiedAt
) {

    /**
     * Simple data mapper from the User aggregate
     *
     * @param user {@link User} The user stored in the database
     * @return New UserCreateResponse with the user information
     */
    public static UserCreateResponse from(final User user) {
        return new UserCreateResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getCreatedAt(),
                user.getModifiedAt()
        );
    }
}
