package com.andreiromila.vetl.role.web;

import com.andreiromila.vetl.role.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Rest controller for retrieving role-related information.
 */
@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    /**
     * The service for role-related operations.
     */
    private final RoleService roleService;

    /**
     * Constructs the RoleController with the required service.
     *
     * @param roleService {@link RoleService} The service for role-related operations.
     */
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Retrieves a list of all available roles in the system.
     * This endpoint is accessible to any authenticated user to populate UI elements,
     * such as role selection dropdowns.
     *
     * @return A {@link ResponseEntity} containing a {@link List} of {@link RoleView} DTOs.
     */
    @GetMapping
    public ResponseEntity<List<RoleView>> getAllRoles() {

        // Get all roles from the database
        final List<RoleView> roles = roleService.findAll().stream()
                .map(RoleView::from)
                .toList();

        // Return the list of DTOs with a 200 OK status
        return ResponseEntity.ok(roles);

    }
}
