package com.andreiromila.vetl.role;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for handling business logic related to roles.
 * This service acts as an intermediary between the controller and the repository.
 */
@Service
public class RoleService {

    /**
     * The data access layer for roles.
     */
    private final RoleRepository roleRepository;

    /**
     * Constructs the RoleService with the required repository.
     *
     * @param roleRepository {@link RoleRepository} The data access layer for roles.
     */
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Retrieves all roles from the database.
     *
     * @return A {@link List} of all {@link Role} entities.
     */
    public List<Role> findAll() {
        return roleRepository.findAll();
    }
}
