package com.andreiromila.vetl.user.web;

import com.andreiromila.vetl.exceptions.HttpBadRequestException;
import com.andreiromila.vetl.responses.CustomPage;
import com.andreiromila.vetl.storage.FileStorageService;
import com.andreiromila.vetl.user.User;
import com.andreiromila.vetl.user.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static com.andreiromila.vetl.utils.PageableUtils.getPageableWithSafeSort;

/**
 * Rest controller for handling user-related HTTP requests.
 * Maps endpoints under the base path `/api/v1/users`.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    /**
     * The allowed sorting columns for the user table
     */
    public static final Set<String> sortingColumns = Set.of("id", "username", "fullName", "email");

    /**
     * Service layer for user operations.
     */
    private final UserService userService;

    /**
     * Service for file storage.
     */
    private final FileStorageService fileStorageService;

    /**
     * Constructs the controller with a dependency-injected UserService.
     *
     * @param userService        {@link UserService} Service layer for user operations.
     * @param fileStorageService {@link FileStorageService} Service for file storage.
     */
    public UserController(final UserService userService, final FileStorageService fileStorageService) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Retrieves the public details of a specific user by their username.
     * This endpoint is accessible to any authenticated user.
     *
     * @param username {@link String} The username of the user to retrieve.
     * @return ResponseEntity containing a {@link UserBasicResponse} with non-sensitive user data.
     */
    @GetMapping("/{username}")
    public ResponseEntity<UserBasicResponse> getAuthenticatedUserDetails(@PathVariable String username) {

        // Find the user using the userDetailsService method
        final User user = userService.loadUserByUsername(username);

        // Return the user
        return ResponseEntity.ok(UserBasicResponse.from(user));

    }

    /**
     * Creates a new user
     *
     * @param request {@link UserCreateRequest} The request body
     * @return Response entity
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserCreateResponse> create(@Valid @RequestBody UserCreateRequest request) {

        // Store the user into the database
        final User user = userService.createUser(request);

        // Create the response body
        final UserCreateResponse responseBody = UserCreateResponse.from(user);

        // Return the entity with the newly created user data
        return ResponseEntity.ok(responseBody);

    }

    /**
     * Searches for users based on a query string and returns a paginated result.
     * The search is performed across multiple fields like username, full name, and email.
     * If the query is null or blank, it returns all users.
     * <p>
     * This method also sanitizes the sorting parameters provided by the client
     * to prevent unauthorized column sorting.
     *
     * @param query    {@link String} An optional search term to filter users. The search is case-insensitive.
     * @param pageable {@link Pageable} A Spring Data Pageable object containing pagination and
     *                 sorting information provided via URL parameters (e.g., ?page=1&size=10&sort=username,asc).
     *
     * @return A {@link ResponseEntity} containing a {@link CustomPage} of {@link UserBasicResponse} objects.
     */
    @GetMapping
    public ResponseEntity<?> search(@RequestParam(value = "query", required = false) String query, Pageable pageable) {

        // Get the pageable with safe sorting
        final Pageable safeSort = getPageableWithSafeSort(pageable, sortingColumns);

        // Get the list of users from the database
        final Page<User> userPage = userService.searchUsers(query, safeSort);

        // Transform the user page to the expected output
        final List<UserBasicResponse> userList = userPage.getContent()
                .stream()
                .map(UserBasicResponse::from)
                .toList();

        // Create the response body
        final CustomPage<UserBasicResponse> response = new CustomPage<>(userList, userPage.getPageable(), userPage.getTotalElements());

        // Return the 200 ok response
        return ResponseEntity.ok(response);

    }

    /**
     * Handles the avatar upload for a specific user, identified by username.
     * Access is restricted to the user themselves or an administrator.
     *
     * @param username {@link String} The username of the user whose avatar is being uploaded.
     * @param file     {@link MultipartFile} The avatar file being uploaded.
     * @return An HTTP 200 OK response on success.
     */
    @PostMapping(path = "/{username}/avatar", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("#username == principal.username or hasRole('ADMIN')")
    public ResponseEntity<Void> uploadAvatarForUser(@PathVariable String username,
                                                    @RequestParam("file") MultipartFile file) {

        // Load the user
        final User user = userService.loadUserByUsername(username);

        // Validate file presence and size (e.g., max 5MB)
        if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024) {
            throw new HttpBadRequestException("File is empty or exceeds the 5MB limit.");
        }

        // Upload the file via the storage service to get its unique key
        String avatarKey = fileStorageService.uploadFile(file);

        // Update the user's record in the database with the new key
        userService.updateAvatarKey(user.getId(), avatarKey);

        // Returns the 201 created
        return ResponseEntity.created(
                URI.create(fileStorageService.getPublicFileUrl(avatarKey))
        ).build();
    }

}
