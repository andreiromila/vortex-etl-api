package com.andreiromila.vetl.user.web;

import com.andreiromila.vetl.user.User;

import java.time.Instant;

/**
 * Data Transfer Object (DTO) representing a subset of user data for API responses.
 * Excludes sensitive fields like passwords. Immutable via Java 16+ record.
 *
 * @param id         {@link Long} Unique user identifier.
 * @param username   {@link String} User's login handle. Unique across the system.
 * @param email      {@link String} User's email address. Unique across the system.
 * @param fullName   {@link String} User's full name, including Unicode characters and spaces.
 * @param createdAt  {@link Instant} Timestamp of user creation (UTC).
 * @param modifiedAt {@link Instant} Timestamp of last user modification (UTC).
 */
public record UserBasicResponse(
        Long id,
        String username,
        String email,
        String fullName,
        Instant createdAt,
        Instant modifiedAt
) {

    /**
     * Factory method to convert a {@link User} entity to a {@link UserBasicResponse}.
     *
     * @param user {@link User} Entity to transform.
     * @return Dto containing non-sensitive user data.
     */
    public static UserBasicResponse from(final User user) {
        return new UserBasicResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getCreatedAt(),
                user.getModifiedAt()
        );
    }
}

