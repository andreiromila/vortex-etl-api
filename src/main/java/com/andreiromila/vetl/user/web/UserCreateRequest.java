package com.andreiromila.vetl.user.web;

import com.andreiromila.vetl.validation.UniqueEmail;
import com.andreiromila.vetl.validation.UniqueUsername;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Request DTO for user creation. Validates input using Jakarta Bean Validation.
 * Annotated with constraints to enforce data integrity before persistence.
 *
 * @param fullName {@link String} User's full name. Allows letters, numbers, accents, apostrophes, and spaces.
 * @param username {@link String} Unique identifier for login. Alphanumeric with underscores/dots, starting with a letter.
 * @param email    {@link String} Valid email address. Enforced to be unique (see commented {@code @UniqueEmail}).
 */
public record UserCreateRequest(

        @NotNull
        @Size(min = 4, max = 100)
        @Pattern(regexp = "^[a-zA-Z0-9'\\sÀ-ÿ.]+$")
        String fullName,

        @NotNull
        @Size(min = 4, max = 100)
        @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_.]+$")
        @UniqueUsername
        String username,

        @NotNull
        @Size(max = 200)
        @Email
        @UniqueEmail
        String email,

        Set<Long> roles

) { }