package com.andreiromila.vetl.user.web;

import com.andreiromila.vetl.validation.PasswordAware;
import com.andreiromila.vetl.validation.PasswordsMatch;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Dto for a user's request to change their own password.
 * Includes validation for a strong new password and
 * ensures the confirmation matches.
 */
@PasswordsMatch
public record UserPasswordChangeRequest(

        /* The current user password to avoid someone with a
         * stolen token changing the user password
         */
        @NotBlank
        String currentPassword,

        /* The password must have at least 1 lower, 1 upper,
         * 1 number and 1 special character and at least 5
         * characters long
         */
        @NotBlank
        @Pattern(regexp = "^((?=\\S*?[A-Z])(?=\\S*?[a-z])(?=\\S*?[0-9]).{5,})\\S$", message = "Password must be strong.")
        String password,

        /* The password must have at least 1 lower, 1 upper,
         * 1 number and 1 special character and at least 5
         * characters long
         */
        @NotBlank
        String passwordConfirmation

) implements PasswordAware { }
