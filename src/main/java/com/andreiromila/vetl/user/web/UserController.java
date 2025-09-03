package com.andreiromila.vetl.user.web;

import com.andreiromila.vetl.responses.CustomPage;
import com.andreiromila.vetl.user.User;
import com.andreiromila.vetl.user.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
     * Constructs the controller with a dependency-injected UserService.
     *
     * @param userService {@link UserService} Service layer for user operations.
     */
    public UserController(final UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves details of the authenticated user.
     *
     * @param user {@link User} Authenticated user resolved via Spring Security's {@link AuthenticationPrincipal}.
     * @return ResponseEntity containing a {@link UserBasicResponse} with non-sensitive user data.
     */
    @GetMapping("/me")
    public ResponseEntity<UserBasicResponse> getAuthenticatedUserDetails(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserBasicResponse.from(user));
    }

    /**
     * Creates a new user
     *
     * @param request {@link UserCreateRequest} The request body
     *
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
}
