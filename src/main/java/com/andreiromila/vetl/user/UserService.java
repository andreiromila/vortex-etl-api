package com.andreiromila.vetl.user;

import com.andreiromila.vetl.role.Role;
import com.andreiromila.vetl.role.RoleRepository;
import com.andreiromila.vetl.role.UserRole;
import com.andreiromila.vetl.storage.FileStorageService;
import com.andreiromila.vetl.user.web.UserCreateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

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
     * Service for file storage operations.
     */
    private final FileStorageService fileStorageService;

    /**
     * Constructs a UserService with required dependencies
     *
     * @param userRepository  {@link UserRepository} The user repository bean
     * @param roleRepository  {@link RoleRepository} The role repository bean
     * @param passwordEncoder {@link PasswordEncoder} The password encoder bean
     * @param fileStorageService {@link FileStorageService} Service for file storage operations.
     */
    public UserService(final UserRepository userRepository,
                       final RoleRepository roleRepository,
                       final PasswordEncoder passwordEncoder,
                       final FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
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

        // Get all role references for the current user
        final Set<UserRole> roleReferences = roleRepository.findUserRoleByUsers(List.of(user.getId()));

        // Find all roles
        final List<Role> userRoles = roleRepository.findAllById(roleReferences.stream().map(UserRole::role).toList());

        // Setup user roles with prefix ROLE_ so we can use hasRole("ADMIN")
        user.setAuthorities(
                userRoles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                        .toList()
        );

        // Set the user roles
        user.setRoles(Set.copyOf(userRoles));

        // Populate the transient avatar URL field
        return userWithAvatarUrl(user);
    }

    /**
     * Creates and persists a new user with system defaults
     *
     * @param request {@link UserCreateRequest} User creation dto containing basic user information
     * @return Persisted user entity with generated fields
     */
    @Transactional
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
        final User savedUser = userRepository.save(user);

        if (request.roles() != null && ! request.roles().isEmpty()) {
            // Now lets save the roles if the user has any
            userRepository.insertUserRoles(savedUser.getId(), request.roles());
        }

        return savedUser;
    }

    /**
     * Searches the database for users based on the provided query
     *
     * @param query {@link String} The string to search for
     * @param pageable {@link Pageable} Pagination data
     * @return The page of matching users
     */
    public Page<User> searchUsers(final String query, final Pageable pageable) {

        // Get the user page first
        final Page<User> userPage;

        if (isNull(query) || query.isBlank()) {
            userPage = userRepository.findAll(pageable);
        } else {
            userPage = userRepository.search(query, pageable);
        }

        // If there are no user there is no need to search for roles
        if (userPage.isEmpty()) {
            return userPage;
        }

        // Get all user_role references based on the user page
        final Set<UserRole> roleReferences = roleRepository.findUserRoleByUsers(
                userPage.get()
                        .map(User::getId)
                        .toList()
        );

        // Now get all the roles for those users
        final List<Role> roles = roleRepository.findAllById(
                roleReferences.stream()
                        .map(UserRole::role)
                        .collect(Collectors.toSet())
        );

        // Lastly we must combine and set the roles for every user
        final List<User> usersWithRoles = userPage.get()
                .map(user -> userWithRoles(user, roleReferences, roles))
                .map(this::userWithAvatarUrl)
                .toList();

        // Create the final page with the users
        return new PageImpl<>(usersWithRoles, userPage.getPageable(), userPage.getTotalElements());
    }

    /**
     * It maps the roles to the user
     *
     * @param user {@link User} The user to associate
     * @param roleReferences {@link Set} The ids of roles
     * @param roles {@link List} The list with all the roles from the database
     * @return The newly created user
     */
    private User userWithRoles(User user, Set<UserRole> roleReferences, List<Role> roles) {

        // Get the role IDs for the current user
        final Set<Long> userRoleIdentifiers = roleReferences.stream()
                .filter(it -> Objects.equals(it.user(), user.getId()))
                .map(UserRole::role)
                .collect(Collectors.toSet());

        // Create the list for the current user
        final Set<Role> userRoles = roles.stream()
                .filter(it -> userRoleIdentifiers.contains(it.id()))
                .collect(Collectors.toSet());

        // Return the new user with its roles
        return user.toBuilder()
                .roles(userRoles)
                .build();
    }

    /**
     * Updates the avatar for a user identified by their username. This method
     * contains the complete, transactional business logic for the operation.
     * It performs a lightweight query to find the user, avoiding the overhead
     * of loading associated roles.
     *
     * @param username {@link String} The username of the user to update.
     * @param file     {@link MultipartFile} The new avatar file.
     *
     * @return The public URL of the newly uploaded avatar.
     *
     * @throws UsernameNotFoundException if a user with the given username does not exist.
     */
    @Transactional
    public String updateAvatarKey(String username, MultipartFile file) {

        // Find the user or throw an exception if not found
        final User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with username: " + username));

        // Get the current avatar key to delete it later
        final String oldAvatarKey = user.getAvatarKey();

        // Upload the new file to get the new key
        String newAvatarKey = fileStorageService.uploadFile(file);

        // Update the database with the new key. This is the core transactional step.
        // Si esta operación falla, toda la transacción hará rollback.
        userRepository.updateAvatarKey(user.getId(), newAvatarKey);

        // If the database update was successful and there was an old avatar, delete it.
        // This operation is outside the core transaction in a sense, but happens only on commit.
        fileStorageService.deleteFile(oldAvatarKey);

        // Return the public URL for the controller to build the response
        return fileStorageService.getPublicFileUrl(newAvatarKey);

    }

    /**
     * Helper method to populate a User object with its public avatar URL.
     *
     * @param user {@link User} The user object to enrich.
     * @return The {@link User} object with the `avatarUrl` field populated.
     */
    private User userWithAvatarUrl(User user) {
        // Check if the user has an associated avatar key
        if (user.getAvatarKey() != null && !user.getAvatarKey().isBlank()) {
            // Generate the public URL using the storage service
            String avatarUrl = fileStorageService.getPublicFileUrl(user.getAvatarKey());
            // Return a new builder-based instance with the URL set
            return user.toBuilder().avatarUrl(avatarUrl).build();
        }
        // Return the original user if no avatar key is present
        return user;
    }
}
