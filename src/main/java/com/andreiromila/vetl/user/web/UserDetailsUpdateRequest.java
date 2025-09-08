package com.andreiromila.vetl.user.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Dto for updating user details.
 * <p>
 * Contains all fields that an admin can modify. When a regular user
 * updates their own profile, only the 'fullName' field is used, while
 * 'roleId' and 'enabled' are ignored by the service logic.
 * The fields are annotated with {@code @NotNull} to ensure administrators
 * always provide a complete and valid state.
 */
public record UserDetailsUpdateRequest(

        @NotNull
        @Size(min = 4, max = 100)
        @Pattern(regexp = "^[a-zA-Z0-9'\\sÀ-ÿ.]+$")
        String fullName,

        @NotNull
        Long roleId,

        @NotNull
        Boolean enabled
) { }