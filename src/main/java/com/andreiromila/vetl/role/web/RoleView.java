package com.andreiromila.vetl.role.web;

import com.andreiromila.vetl.role.Role;

/**
 * A role view for outside usage
 *
 * @param id
 * @param name
 * @param description
 */
public record RoleView(
        Long id,
        String name,
        String description
) {

    /**
     * Creates a new RoleView from an aggregate instance
     *
     * @param role {@link Role} The instance from database
     * @return The new RoleView
     */
    public static RoleView from(final Role role) {
        return new RoleView(
                role.id(),
                role.name(),
                role.description()
        );
    }
}
