package com.andreiromila.vetl.user.web;

import com.andreiromila.vetl.validation.PasswordsMatch;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Dto for the user account activation request.
 * Contains fields for username, token, and password confirmation.
 * This record is validated at the class level to ensure passwords match.
 */
@PasswordsMatch
public record UserActivationRequest(

        @NotBlank
        @Size(min = 64, max = 64)
        String token,

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
) { }
