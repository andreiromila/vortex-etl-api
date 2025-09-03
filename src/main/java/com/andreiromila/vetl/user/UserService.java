package com.andreiromila.vetl.user;

import com.andreiromila.vetl.role.Role;
import com.andreiromila.vetl.role.RoleReference;
import com.andreiromila.vetl.role.RoleRepository;
import com.andreiromila.vetl.user.web.UserCreateRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Service class handling user management
 * operations and Spring Security integration.
 * <p>
 * Implements UserDetailsService to provide
 * user data for authentication/authorization.
 */
@Service
public class UserService implements UserDetailsService {

    /**
     * Data access component for user-related database operations
     */
    private final UserRepository userRepository;

    /**
     * Role repository bean
     */
    private final RoleRepository roleRepository;

    /**
     * Component for password encryption and validation
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a UserService with required dependencies
     *
     * @param userRepository  {@link UserRepository} The user repository bean
     * @param roleRepository  {@link RoleRepository} The role repository bean
     * @param passwordEncoder {@link PasswordEncoder} The password encoder bean
     */
    public UserService(final UserRepository userRepository, final RoleRepository roleRepository, final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Loads a user from the database by username
     *
     * @param username {@link String} The username to search for
     * @return The UserDetails
     * @throws UsernameNotFoundException If the user was not found
     */
    @Override
    public User loadUserByUsername(final String username) throws UsernameNotFoundException {
        final User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with username: " + username));

        // Find all roles
        final List<Role> userRoles = roleRepository.findAllById(user.getRoles().stream().map(RoleReference::role).toList());

        // Setup user roles with prefix ROLE_ so we can use hasRole("ADMIN")
        user.setAuthorities(
                userRoles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                        .toList()
        );

        return user;
    }

    /**
     * Creates and persists a new user with system defaults
     *
     * @param request {@link UserCreateRequest} User creation dto containing basic user information
     * @return Persisted user entity with generated fields
     */
    public User createUser(final UserCreateRequest request) {

        // Create the user aggregate
        final User user = new User();

        // Set basic fields
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());

        // Security flags
        user.setEnabled(true);

        // Timestamps
        user.setCreatedAt(Instant.now());
        user.setModifiedAt(Instant.now());

        // No default roles
        return userRepository.save(user);

    }

}
