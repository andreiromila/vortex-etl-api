package com.andreiromila.vetl.token;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Represents an authentication token entity with expiration tracking.
 * Immutable record using Lombok's builder pattern for object creation.
 *
 * @param uuid      {@link String} Unique identifier (primary key) generated before persistence
 * @param username  {@link String} Associated user reference
 * @param userAgent {@link String} Client device/browser identification for token binding
 * @param enable    {@link Boolean} Activation status flag for token revocation
 * @param expiresAt {@link Instant} Token validity expiration timestamp
 */
@Builder(toBuilder = true)
@Table("token")
public record Token(

        @Id
        String uuid,

        String username,

        String userAgent,

        boolean enable,

        Instant expiresAt
) { }