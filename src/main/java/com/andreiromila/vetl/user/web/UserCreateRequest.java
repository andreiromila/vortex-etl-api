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
 * @param password {@link String} Password with complexity: 1 lowercase, 1 uppercase, 1 digit, 1 special character, min 5 length.
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

        /* The password must have at least 1 lower, 1 upper,
         * 1 number and 1 special character and at least 5
         * characters long
         */
        @NotNull
        @Pattern(regexp = "^((?=\\S*?[A-Z])(?=\\S*?[a-z])(?=\\S*?[0-9]).{5,})\\S$")
        String password,

        Set<Long> roles

) { }